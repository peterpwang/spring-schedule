const React = require('react');
const ReactDOM = require('react-dom');
const when = require('when');
const client = require('./client');
import Cookies from 'universal-cookie';
import LoginContext from './logincontext';

const root = '/users';
const rootApi = root + '/api';
const cookies = new Cookies();
//var stompClient = require('./websocket-listener')

class UserApp extends React.Component {

	constructor(props) {
		super(props);
		this.state = {
			users: [], 
			attributes: [], 
			page: 1, 
			pageSize: 10, 
			links: {}, 
			loggedInManager: this.props.loggedInManager, 
			csrfToken: undefined};
		this.updatePageSize = this.updatePageSize.bind(this);
		this.onCreate = this.onCreate.bind(this);
		this.onUpdate = this.onUpdate.bind(this);
		this.onDelete = this.onDelete.bind(this);
		this.onNavigate = this.onNavigate.bind(this);
		this.refreshCurrentPage = this.refreshCurrentPage.bind(this);
		this.refreshAndGoToLastPage = this.refreshAndGoToLastPage.bind(this);
		
		if(cookies.get('XSRF-TOKEN')) {
			this.state.csrfToken = cookies.get('XSRF-TOKEN');
		}
	}

	componentDidMount() {
		this.loadFromServer(this.state.pageSize);
		/* zuul not support yet */
		/*stompClient.register(root, [
			{route: '/topic/newUser', callback: this.refreshAndGoToLastPage},
			{route: '/topic/updateUser', callback: this.refreshCurrentPage},
			{route: '/topic/deleteUser', callback: this.refreshCurrentPage}
		]);*/
	}

	render() {
		return (
			<div>
				<CreateDialog attributes={this.state.attributes} onCreate={this.onCreate}/>
				<UserList page={this.state.page}
							users={this.state.users}
							links={this.state.links}
							pageSize={this.state.pageSize}
							attributes={this.state.attributes}
							onNavigate={this.onNavigate}
							onUpdate={this.onUpdate}
							onDelete={this.onDelete}
							updatePageSize={this.updatePageSize}
							loggedInManager={this.state.loggedInManager}/>
			</div>
		)
	}
	
	loadFromServer(pageSize) {
		client({
			method: 'GET',
			path: rootApi + '/users?size=' + pageSize,
			credentials: 'include',
			headers: {
				'Content-Type': 'application/json',
				'X-XSRF-TOKEN': this.state.csrfToken,
				'Authorization': this.context.authorization
			}
		}).then(userCollection => {
			return client({                               // 3. visit paging links profile /api/profile/users
				method: 'GET',
				path: userCollection.entity._links.profile.href,
				headers: {
					'Accept': 'application/schema+json',
					'Authorization': this.context.authorization
				}
			}).then(schema => {                           // 4. pick up schema information from result
				
				/**
				 * Filter unneeded JSON Schema properties, like uri references and
				 * subtypes ($ref).
				 */
				Object.keys(schema.entity.properties).forEach(function (property) {
					if (schema.entity.properties[property].hasOwnProperty('format') &&
						schema.entity.properties[property].format === 'uri') {
						delete schema.entity.properties[property];
					}
					else if (schema.entity.properties[property].hasOwnProperty('$ref')) {
						delete schema.entity.properties[property];
					}
				});

				this.schema = schema.entity;
				this.links = userCollection.entity._links;
				return userCollection;
			});
		}).then(userCollection => {
			this.page = userCollection.entity.page;       // 5. save pagable information
			return userCollection.entity._embedded.users.map(user =>
					client({                              // 6. visit every user detail
						method: 'GET',
						path: user._links.self.href,
						headers: {'Authorization': this.context.authorization}
					})
			);
		}).then(userPromises => {
			return when.all(userPromises);
		}).done(users => {
			this.setState({                               // 7. save users, fields, size, paging links into state
				page: this.page,
				users: users,
				attributes: Object.keys(this.schema.properties),
				pageSize: pageSize,
				links: this.links
			});
		});
	}
	
	onCreate(newUser) {
		client({
			method: 'POST',
			path: rootApi + '/users',
			entity: newUser,
			credentials: 'include',
			headers: {
				'Content-Type': 'application/json',
				'X-XSRF-TOKEN': this.state.csrfToken,
				'Authorization': this.context.authorization
			}
		}).done(response => {
			/* Let the websocket handler update the state */
			/* zuul not support yet. Refresh and go to last page */
			this.refreshAndGoToLastPage();
			// Hide the dialog
			$('#createUser').modal('hide');
		}, response => {
			if (response.status.code === 403) {
				alert('ACCESS DENIED: You are not authorized to update.');
			} 
			else if (response.status.code === 500) {
				alert('Server error.');
			}
			else if (response.status.code === 400) {
				alert('Invalid data: ' + response.entity);
			}
		});
	}
	
	onUpdate(user, updatedUser, index) {
		if(user.entity.manager === undefined || user.entity.manager.name === this.state.loggedInManager) {
			updatedUser["manager"] = user.entity.manager;
			
			client({
				method: 'PUT',
				path: user.entity._links.self.href,
				entity: updatedUser,
				credentials: 'include',
				headers: {
					'Content-Type': 'application/json',
					'If-Match': user.headers.Etag,
					'X-XSRF-TOKEN': this.state.csrfToken,
					'Authorization': this.context.authorization
				}
			}).done(response => {
				/* Let the websocket handler update the state */
				/* zuul not support yet. Refresh current page */
				this.refreshCurrentPage();
				// Hide the dialog
				$('#update_' + index).modal('hide');
			}, response => {
				if (response.status.code === 403) {
					alert('ACCESS DENIED: You are not authorized to update ' +
						user.entity._links.self.href);
				} 
				else if (response.status.code === 412) {
					alert('DENIED: Unable to update ' +
						user.entity._links.self.href + '. Your copy is stale.');
				}
				else if (response.status.code === 500) {
					alert('Server error. ' + user.entity._links.self.href);
				}
				else if (response.status.code === 400) {
					alert('Invalid data: ' + response.entity);
				}
			});
		} 
		else {
			alert("You are not authorized to update. You have to be the manager of the current user.");
		}
	}
	
	onDelete(user) {
		client({
			method: 'DELETE', 
			path: user.entity._links.self.href,
			credentials: 'include',
			headers: {
				'If-Match': user.headers.Etag,
				'X-XSRF-TOKEN': this.state.csrfToken,
				'Authorization': this.context.authorization
			}
		}).done(response => {
				/* let the websocket handle updating the UI */
				/* zuul not support yet. Refresh current page */
				this.refreshCurrentPage();
			},
			response => {
				if (response.status.code === 403) {
					alert('ACCESS DENIED: You are not authorized to delete ' +
						user.entity._links.self.href);
				}
				else if (response.status.code === 500) {
					alert('Server error. ' + user.entity._links.self.href);
				}
				else if (response.status.code === 400) {
					alert('Invalid data: ' + response.entity);
				}
			});
	}
	
	onNavigate(navUri) {
		client({
			method: 'GET', 
			path: navUri,
			headers: {'Authorization': this.context.authorization}
		}).then(userCollection => {
			this.links = userCollection.entity._links;
			this.page = userCollection.entity.page;
			
			return userCollection.entity._embedded.users.map(user =>
			        client({
						method: 'GET',
						path: user._links.self.href,
						headers: {'Authorization': this.context.authorization}
					})
			);
		}).then(userPromises => {
			return when.all(userPromises);
		}).done(users => {
			this.setState({
				page: this.page,
				users: users,
				attributes: Object.keys(this.schema.properties),
				pageSize: this.state.pageSize,
				links: this.links
			});
		});
	}
	
	updatePageSize(pageSize) {
		if (pageSize !== this.state.pageSize) {
			this.loadFromServer(pageSize);
		}
	}
	
	refreshAndGoToLastPage() {
		client({
			method: 'GET', 
			path: rootApi + "/users?size=" + this.state.pageSize,
			headers: {'Authorization': this.context.authorization}
		}).done(response => {
			if (response.entity._links.last !== undefined) {
				this.onNavigate(response.entity._links.last.href);
			} else {
				this.onNavigate(response.entity._links.self.href);
			}
		});
	}

	refreshCurrentPage() {
		client({
			method: 'GET', 
			path: rootApi + "/users?size=" + this.state.pageSize + "&page=" + this.state.page.number,
			headers: {'Authorization': this.context.authorization}
		}).then(userCollection => {
			this.links = userCollection.entity._links;
			this.page = userCollection.entity.page;

			if (userCollection.entity._embedded != undefined && userCollection.entity._embedded.users != undefined) {
				return userCollection.entity._embedded.users.map(user => {
					return client({
						method: 'GET',
						path: user._links.self.href,
						headers: {'Authorization': this.context.authorization}
					})
				});
			}
			else {
				return Promise.reject('No data found');
			}
		}).then(userPromises => {
			return when.all(userPromises);
		}).then(users => {
			this.setState({
				page: this.page,
				users: users,
				attributes: Object.keys(this.schema.properties),
				pageSize: this.state.pageSize,
				links: this.links
			});
		}).catch(e => {
			this.setState({
				page: 1, 
				users: [], 
				attributes: [],
				pageSize: this.state.pageSize,
				links: {}
			});
		});
	}
}

class UserList extends React.Component{

	constructor(props) {
		super(props);
		this.handleNavFirst = this.handleNavFirst.bind(this);
		this.handleNavPrev = this.handleNavPrev.bind(this);
		this.handleNavNext = this.handleNavNext.bind(this);
		this.handleNavLast = this.handleNavLast.bind(this);
		this.handleInput = this.handleInput.bind(this);
	}

	handleNavFirst(e){
		e.preventDefault();
		this.props.onNavigate(this.props.links.first.href);
	}

	handleNavPrev(e) {
		e.preventDefault();
		this.props.onNavigate(this.props.links.prev.href);
	}

	handleNavNext(e) {
		e.preventDefault();
		this.props.onNavigate(this.props.links.next.href);
	}

	handleNavLast(e) {
		e.preventDefault();
		this.props.onNavigate(this.props.links.last.href);
	}
	
	handleInput(e) {
		e.preventDefault();
		const pageSize = ReactDOM.findDOMNode(this.refs.pageSize).value;
		if (/^[0-9]+$/.test(pageSize)) {
			this.props.updatePageSize(pageSize);
		} else {
			ReactDOM.findDOMNode(this.refs.pageSize).value =
				pageSize.substring(0, pageSize.length - 1);
		}
	}

	render() {

		const pageInfo = this.props.page.hasOwnProperty("number") ?
			<h3>Users - Page {this.props.page.number + 1} of {this.props.page.totalPages}</h3> : null;

		const users = this.props.users.map((user, index) =>
			<User key={user.entity._links.self.href} 
			        user={user} 
					attributes={this.props.attributes}
					onUpdate={this.props.onUpdate}
					onDelete={this.props.onDelete}
					index={index}
					loggedInManager={this.props.loggedInManager}/>
		);

		const navLinks = [];
		if ("first" in this.props.links) {
			navLinks.push(<button className="btn btn-light" key="first" onClick={this.handleNavFirst}>&lt;&lt;</button>);
		}
		if ("prev" in this.props.links) {
			navLinks.push(<button className="btn btn-light" key="prev" onClick={this.handleNavPrev}>&lt;</button>);
		}
		if ("next" in this.props.links) {
			navLinks.push(<button className="btn btn-light" key="next" onClick={this.handleNavNext}>&gt;</button>);
		}
		if ("last" in this.props.links) {
			navLinks.push(<button className="btn btn-light" key="last" onClick={this.handleNavLast}>&gt;&gt;</button>);
		}

		return (
			<div>
                {pageInfo}
				<p><input ref="pageSize" defaultValue={this.props.pageSize} onInput={this.handleInput}/> users per page</p>
				<table className="table">
					<tbody>
						<tr>
							<th>Name</th>
							<th>Active</th>
							<th>Description</th>
							<th>Manager</th>
							<th></th>
						</tr>
						{users}
					</tbody>
				</table>
				<div>
					{navLinks}
				</div>
			</div>
		)
	}
}

class User extends React.Component{

	constructor(props) {
		super(props);
		this.handleDelete = this.handleDelete.bind(this);
		if (this.props.user.entity.manager !== undefined) {
			this.state = {managername: this.props.user.entity.manager.name};
		}
		else {
			this.state = {managername: ""};
		}
	}

	handleDelete() {
		this.props.onDelete(this.props.user);
	}

	render() {
		return (
			<tr>
				<td>{this.props.user.entity.name}</td>
				<td>{this.props.user.entity.active}</td>
				<td>{this.props.user.entity.description}</td>
				<td>{this.state.managername}</td>
				<td>
						<UpdateDialog user={this.props.user}
								  attributes={this.props.attributes}
								  onUpdate={this.props.onUpdate}
								  index={this.props.index}
								  loggedInManager={this.props.loggedInManager}/>
					| <span><a href="#" onClick={this.handleDelete}>Delete</a></span>
				</td>
			</tr>
		)
	}
}

class CreateDialog extends React.Component {

	constructor(props) {
		super(props);
		this.handleSubmit = this.handleSubmit.bind(this);
		this.handleActiveChange = this.handleActiveChange.bind(this);

		// clear out the dialog's inputs
		//this.props.attributes.forEach(attribute => {
		//	ReactDOM.findDOMNode(this.refs[attribute]).value = '';
		//});
		
		this.state = {active: "1"};
	}

	handleActiveChange(e) {
	  this.setState({active: e.target.value});
	};

	handleSubmit(e) {
		e.preventDefault();
		
		const form = ReactDOM.findDOMNode(this.refs["form"]);
		if (form.checkValidity()) {
			const newUser = {};
			this.props.attributes.forEach(attribute => {
				if (attribute != "active") {
					newUser[attribute] = ReactDOM.findDOMNode(this.refs[attribute]).value.trim();
				}
				else {
					newUser[attribute] = this.state.active;
				}
			});
			this.props.onCreate(newUser);
		}
		else {
			form.querySelector('input[type="submit"]').click();
		}
	}

	render() {
		const inputs = (
			<div>
				<input type="hidden" ref="id" value=""/>
				<div key="name" className="form-group">
					<label htmlFor="name">Name</label>
					<input type="text" ref="name" className="form-control" placeholder="name" required minLength="1" maxLength="60"/>
				</div>
				<div key="password" className="form-group">
					<label htmlFor="password">Password</label>
					<input type="password" ref="password" className="form-control" placeholder="password" required minLength="1" maxLength="60"/>
				</div>
				<div key="passwordRepeat" className="form-group">
					<label htmlFor="passwordRepeat">Repeat password</label>
					<input type="password" ref="passwordRepeat" className="form-control" placeholder="repeat password" required minLength="1" maxLength="60"/>
				</div>
				<div key="active" className="form-group">
					Active
					<label><input name="active" type="radio" value="1" checked={this.state.active == "1"} onChange={this.handleActiveChange}/> Yes</label>
					<label><input name="active" type="radio" value="0" checked={this.state.active == "0"} onChange={this.handleActiveChange}/> No</label>
				</div>
				<div key="description" className="form-group">
					<label htmlFor="description">Description</label>
					<textarea ref="description" className="form-control" placeholder="description" maxLength="60"/>
				</div>
			</div>
		);

		return (
			<div>
				<a href="#createUser" data-toggle="modal" data-target="#createUser">Create</a>
				<div className="modal fade" id="createUser">
				  <div className="modal-dialog">
					<div className="modal-content">
					  {/* Modal Header */}
					  <div className="modal-header">
						<h4 className="modal-title">Create new user</h4>
						<button type="button" className="close" data-dismiss="modal">&times;</button>
					  </div>
					  {/* Modal body */}
					  <form ref="form">
					    <div className="modal-body">
							{inputs}
					    </div>
					    {/* Modal footer */}
					    <div className="modal-footer">
						  <button type="button" className="btn" onClick={this.handleSubmit}>Create</button>
						  <button type="button" className="btn" data-dismiss="modal">Close</button>
						  <input type="submit" ref="submit" className="hiddensubmit" value="Create"/>
					    </div>
					  </form>
					</div>
				  </div>
				</div>
			</div>
		)
	}
}

class UpdateDialog extends React.Component {

	constructor(props) {
		super(props);
		this.handleSubmit = this.handleSubmit.bind(this);
		this.handleActiveChange = this.handleActiveChange.bind(this);
		
		this.state = {active: this.props.user.entity["active"]};
	}

	handleActiveChange(e) {
	  this.setState({active: e.target.value});
	};

	handleSubmit(e) {
		e.preventDefault();
		
		const form = ReactDOM.findDOMNode(this.refs["form"]);
		if (form.checkValidity()) {
			const updatedUser = {};
			this.props.attributes.forEach(attribute => {
				if (attribute != "active") {
					updatedUser[attribute] = ReactDOM.findDOMNode(this.refs[attribute]).value.trim();
				}
				else {
					updatedUser[attribute] = this.state.active;
				}
			});
			this.props.onUpdate(this.props.user, updatedUser, this.props.index);
		}
		else {
			form.querySelector('input[type="submit"]').click();
		}
	}

	render() {
		const inputs = (
			<div>
				<input type="hidden" ref="id" value={this.props.user.entity["id"]}/>
				<div key="name" className="form-group">
					<label htmlFor="name">Name</label>
					<input type="text" ref="name" className="form-control" placeholder="name" required minLength="1" maxLength="60" defaultValue={this.props.user.entity["name"]}/>
				</div>
				<div key="password" className="form-group">
					<label htmlFor="password">Password</label>
					<input type="password" ref="password" className="form-control" placeholder="password" required minLength="1" maxLength="60" defaultValue={this.props.user.entity["password"]}/>
				</div>
				<div key="passwordRepeat" className="form-group">
					<label htmlFor="passwordRepeat">Repeat password</label>
					<input type="password" ref="passwordRepeat" className="form-control" placeholder="repeat password" required minLength="1" maxLength="60" defaultValue={this.props.user.entity["passwordRepeat"]}/>
				</div>
				<div key="active" className="form-group">
					Active
					<label><input name="active" type="radio" value="1" checked={this.state.active == "1"} onChange={this.handleActiveChange}/> Yes</label>
					<label><input name="active" type="radio" value="0" checked={this.state.active == "0"} onChange={this.handleActiveChange}/> No</label>
				</div>
				<div key="description" className="form-group">
					<label htmlFor="description">Description</label>
					<input type="text" ref="description" className="form-control" placeholder="description" maxLength="60" defaultValue={this.props.user.entity["description"]}/>
				</div>
			</div>
		);

		return (
			<span key={this.props.user.entity._links.self.href}>
				<a href={"#update_" + this.props.index} data-toggle="modal" data-target={"#update_" + this.props.index}>Update</a>
				<div className="modal fade" id={"update_" + this.props.index}>
				  <div className="modal-dialog">
					<div className="modal-content">
					  {/* Modal Header */}
					  <div className="modal-header">
						<h4 className="modal-title">Update a user</h4>
						<button type="button" className="close" data-dismiss="modal">&times;</button>
					  </div>
					  {/* Modal body */}
					  <form ref="form">
					    <div className="modal-body">
							{inputs}
  					    </div>
					    {/* Modal footer */}
					    <div className="modal-footer">
						  <button type="button" className="btn" onClick={this.handleSubmit}>Update</button>
  						  <button type="button" className="btn" data-dismiss="modal">Close</button>
						  <input type="submit" ref="submit" className="hiddensubmit" value="Update"/>
					    </div>
					  </form>
					</div>
				  </div>
				</div>
			</span>
		)
	}
};

UserApp.contextType = LoginContext; // This part is important to access context values

export default UserApp;
