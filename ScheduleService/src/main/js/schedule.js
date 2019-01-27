const React = require('react');
const ReactDOM = require('react-dom');
const when = require('when');
const client = require('./client');
const follow = require('./follow');
import Cookies from 'universal-cookie';

const cookies = new Cookies();
const root = '/api';
const schedulePath = 'schedules';
const scheduleByNamePath = 'schedulesByName';
const userPath = 'users';
const scheduleAttributes = ["name", "description", "active", "user"]; //, "dateSchedule", "timeStart", "timeEnd"];

class ScheduleApp extends React.Component {

	constructor(props) {
		super(props);
		this.state = {
			schedules: [], 
			attributes: scheduleAttributes, 
			page: 1, 
			pageSize: 10, 
			searchKeyword: undefined,
			links: {}, 
			users: [],
			loggedInManager: this.props.loggedInManager, 
			csrfToken: undefined};
		this.updatePageSize = this.updatePageSize.bind(this);
		this.onCreate = this.onCreate.bind(this);
		this.onUpdate = this.onUpdate.bind(this);
		this.onDelete = this.onDelete.bind(this);
		this.onNavigate = this.onNavigate.bind(this);
		this.refreshCurrentPage = this.refreshCurrentPage.bind(this);
		this.refreshAndGoToLastPage = this.refreshAndGoToLastPage.bind(this);
		this.searchByName = this.searchByName.bind(this);
		
		if(cookies.get('XSRF-TOKEN')) {
			this.state.csrfToken = cookies.get('XSRF-TOKEN');
		}
		
		// Get all users
		client({                                           // 1. visit users link
			method: 'GET',
			path: root + "/" + userPath,
			headers: {'Accept': 'application/hal+json'}
		}).then(userCollection => {
			this.state.users = userCollection.entity._embedded.users;                               // 2. save users
		});
	}

	componentDidMount() {
		this.loadFromServer(this.state.pageSize);
	}

	render() {
		return (
			<div>
				<CreateDialog attributes={this.state.attributes} users={this.state.users} onCreate={this.onCreate}/>
				<ScheduleList page={this.state.page}
							schedules={this.state.schedules}
							links={this.state.links}
							pageSize={this.state.pageSize}
							attributes={this.state.attributes}
							users={this.state.users}
							onNavigate={this.onNavigate}
							onUpdate={this.onUpdate}
							onDelete={this.onDelete}
							updatePageSize={this.updatePageSize}
							searchByName={this.searchByName}
							loggedInManager={this.state.loggedInManager}/>
			</div>
		)
	}
	
	loadFromServer(pageSize) {
		loadFromServer(pageSize, undefined);
	}
	
	loadFromServer(pageSize, name) {
		const path = (name == undefined || name.length == 0) ? schedulePath : scheduleByNamePath;
		const nameParam = (name == undefined || name.length == 0) ? '' : '&name=' + name;
		
		client({                                           // 1. visit schedules link with size parameter
			method: 'GET',
			path: root + "/" + path + "?size=" + pageSize + nameParam,
			headers: {'Accept': 'application/hal+json'}
		}).then(scheduleCollection => {
				this.links = scheduleCollection.entity._links;
				this.page = scheduleCollection.entity.page;
				return scheduleCollection;
		}).then(scheduleCollection => {
			if (scheduleCollection.entity._embedded) {
				return scheduleCollection.entity._embedded.scheduleResources.map(schedule =>
					client({                              // 2. visit every schedule detail
						method: 'GET',
						path: schedule._links.self.href
					})
				);
			}
			else {
				return Promise.reject('No schedules data found');
			}
		}).then(schedulePromises => {
			return when.all(schedulePromises);
		}).then(schedules => {
			this.setState({                               // 3. save schedules, fields, size, paging links
				links: this.links,
				page: this.page,
				schedules: schedules,
				pageSize: pageSize,
				searchKeyword: name
			});
		}).catch(e => {
			this.setState({
				links: {},
				page: 1, 
				schedules: [],
				pageSize: this.state.pageSize
			});
		});
	}
	
	onCreate(newSchedule) {
		this.state.users.forEach(user => {
			if (user._links.self.href == newSchedule.user) {
				newSchedule.user = user;
			}
		});

		follow(client, root, [{rel: 'schedules'}]).then(response => {
			client({
				method: 'POST',
				path: response.entity._links.self.href,
				entity: newSchedule,
				credentials: 'include',
				headers: {
					'Content-Type': 'application/json',
					'X-XSRF-TOKEN': this.state.csrfToken
				}
			}).then(response => {
				/* refresh and go to last page */
				this.refreshAndGoToLastPage();

				// Hide the dialog
				$('#createSchedule').modal('hide');
			}).catch(response => {
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
		});
	}
	
	onUpdate(schedule, updatedSchedule, index) {
		if(schedule.entity.manager === undefined || schedule.entity.manager.name === this.state.loggedInManager) {
			updatedSchedule["manager"] = schedule.entity.manager;
			
			client({
				method: 'PUT',
				path: schedule.entity._links.self.href,
				entity: updatedSchedule,
				credentials: 'include',
				headers: {
					'Content-Type': 'application/json',
					'If-Match': schedule.headers.Etag,
					'X-XSRF-TOKEN': this.state.csrfToken
				}
			}).then(response => {
				/* Refresh current page */
				this.refreshCurrentPage();
				// Hide the dialog
				$('#update_' + index).modal('hide');
			}).catch(response => {
				if (response.status.code === 403) {
					alert('ACCESS DENIED: You are not authorized to update ' +
						schedule.entity._links.self.href);
				} 
				else if (response.status.code === 412) {
					alert('DENIED: Unable to update ' +
						schedule.entity._links.self.href + '. Your copy is stale.');
				}
				else if (response.status.code === 500) {
					alert('Server error. ' + schedule.entity._links.self.href);
				}
				else if (response.status.code === 400) {
					alert('Invalid data: ' + response.entity);
				}
			});
		} 
		else {
			alert("You are not authorized to update. You have to be the manager of the current schedule.");
		}
	}
	
	onDelete(schedule) {
		client({
			method: 'DELETE', 
			path: schedule.entity._links.self.href,
			credentials: 'include',
			headers: {
				'If-Match': schedule.headers.Etag,
				'X-XSRF-TOKEN': this.state.csrfToken
			}
		}).then(response => {
			/* Refresh current page */
			this.refreshCurrentPage();
		}).catch(response => {
			if (response.status.code === 403) {
				alert('ACCESS DENIED: You are not authorized to delete ' +
					schedule.entity._links.self.href);
			}
			else if (response.status.code === 500) {
				alert('Server error. ' + schedule.entity._links.self.href);
			}
			else if (response.status.code === 400) {
				alert('Invalid data: ' + response.entity);
			}
		});
	}
	
	onNavigate(navUri) {
		client({
			method: 'GET', 
			path: navUri
		}).then(scheduleCollection => {
			this.links = scheduleCollection.entity._links;
			this.page = scheduleCollection.entity.page;
			
			return scheduleCollection.entity._embedded.scheduleResources.map(schedule =>
			        client({
						method: 'GET',
						path: schedule._links.self.href
					})
			);
		}).then(schedulePromises => {
			return when.all(schedulePromises);
		}).then(schedules => {
			this.setState({
				page: this.page,
				schedules: schedules,
				attributes: scheduleAttributes,
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
		follow(client, root, [{
			rel: 'schedules',
			params: {size: this.state.pageSize}
		}]).then(response => {
			if (response.entity._links.last !== undefined) {
				this.onNavigate(response.entity._links.last.href);
			} else {
				this.onNavigate(response.entity._links.self.href);
			}
		})
	}

	refreshCurrentPage() {
		follow(client, root, [{
			rel: 'schedules',
			params: {
				size: this.state.pageSize,
				page: this.state.page.number
			}
		}]).then(scheduleCollection => {
			this.links = scheduleCollection.entity._links;
			this.page = scheduleCollection.entity.page;

			if (scheduleCollection.entity._embedded != undefined && scheduleCollection.entity._embedded.scheduleResources != undefined) {
				return scheduleCollection.entity._embedded.scheduleResources.map(schedule => {
					return client({
						method: 'GET',
						path: schedule._links.self.href
					})
				});
			}
			else {
				return Promise.reject('No data found');
			}
		}).then(schedulePromises => {
			return when.all(schedulePromises);
		}).then(schedules => {
			this.setState({
				page: this.page,
				schedules: schedules,
				attributes: scheduleAttributes,
				pageSize: this.state.pageSize,
				links: this.links
			});
		}).catch(e => {
			this.setState({
				page: 1, 
				schedules: [],
				attributes: scheduleAttributes,
				pageSize: this.state.pageSize,
				links: {}
			});
		});
	}
	
	searchByName(name) {
		if (name !== this.state.searchKeyword) {
			this.loadFromServer(this.state.pageSize, name);
		}
	}
}

class SearchForm extends React.Component{

	constructor(props) {
		super(props);
		this.handleSubmit = this.handleSubmit.bind(this);
	}
	
	handleSubmit(e) {
		e.preventDefault();
		const name = ReactDOM.findDOMNode(this.refs.name).value;
		if (name != undefined && name.length > 0) {
			this.props.searchByName(name);
		}
	}

	render() {
		return (
			<form ref="form">
				Search for: <input ref="name" defaultValue={this.props.searchKeyword} onSubmit={this.handleSubmit}/>
			    <input type="submit" className="btn btn-light" key="searchname" onClick={this.handleSubmit} value="Search"/>
  		    </form>
		)
	}
}

class ScheduleList extends React.Component{

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
			<h3>Schedules - Page {this.props.page.number + 1} of {this.props.page.totalPages}</h3> : null;

		const schedules = this.props.schedules.map((schedule, index) =>
			<Schedule key={schedule.entity._links.self.href} 
			        schedule={schedule} 
					attributes={this.props.attributes}
					users={this.props.users}
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
				<p><input ref="pageSize" defaultValue={this.props.pageSize} onInput={this.handleInput}/> schedules per page</p>
				<SearchForm searchByName={this.props.searchByName}/>
				<table className="table">
					<tbody>
						<tr>
							<th>Name</th>
							<th>Active</th>
							<th>Description</th>
							<th>Manager</th>
							<th></th>
						</tr>
						{schedules}
					</tbody>
				</table>
				<div>
					{navLinks}
				</div>
			</div>
		)
	}
}

class Schedule extends React.Component{

	constructor(props) {
		super(props);
		this.handleDelete = this.handleDelete.bind(this);
		if (this.props.schedule.entity.manager !== undefined) {
			this.state = {managername: this.props.schedule.entity.manager.name};
		}
		else {
			this.state = {managername: ""};
		}
	}

	handleDelete() {
		this.props.onDelete(this.props.schedule);
	}

	render() {
		return (
			<tr>
				<td>{this.props.schedule.entity.name}</td>
				<td>{this.props.schedule.entity.active}</td>
				<td>{this.props.schedule.entity.description}</td>
				<td>{this.state.managername}</td>
				<td>
					<UpdateDialog schedule={this.props.schedule}
								  attributes={this.props.attributes}
								  users={this.props.users}
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
		this.handleUserChange = this.handleUserChange.bind(this);

		// clear out the dialog's inputs
		//this.props.attributes.forEach(attribute => {
		//	ReactDOM.findDOMNode(this.refs[attribute]).value = '';
		//});
		
		this.state = {
			active: "1",
		};
	}

	handleActiveChange(e) {
	  this.setState({active: e.target.value});
	};

	handleUserChange(e) {
		this.setState({selectedUser: e.target.value});
	};

	handleSubmit(e) {
		e.preventDefault();
		
		const form = ReactDOM.findDOMNode(this.refs["form"]);
		if (form.checkValidity()) {
			const newSchedule = {};
			this.props.attributes.forEach(attribute => {
				if (attribute == "active") {
					newSchedule[attribute] = this.state.active;
				}
				else if (attribute == "user") {
					const id = this.state.selectedUser;
					this.props.users.forEach(user => {
						if (id == user.id) {
							newSchedule[attribute] = user;
						}
					});
				}
				else {
					newSchedule[attribute] = ReactDOM.findDOMNode(this.refs[attribute]).value.trim();
				}
			});
			this.props.onCreate(newSchedule);
		}
		else {
			form.querySelector('input[type="submit"]').click();
		}
	}

	render() {
		let options;
		if (this.props.users != undefined) {
			options = this.props.users.map((user) =>
				<option key={"usercreate" + user.id} value={user.id}>{user.name}</option>
			);
		}

		const inputs = (
			<div>
				<input type="hidden" ref="id" value=""/>
				<div key="name" className="form-group">
					<label htmlFor="name">Name</label>
					<input type="text" ref="name" className="form-control" placeholder="name" required minLength="1" maxLength="60"/>
				</div>
				<div key="active" className="form-group">
					Active
					<label><input name="active" type="radio" value="1" checked={this.state.active == "1"} onChange={this.handleActiveChange}/> Yes</label>
					<label><input name="active" type="radio" value="0" checked={this.state.active == "0"} onChange={this.handleActiveChange}/> No</label>
				</div>
				<div key="user" className="form-group">
				    <label htmlFor="user">User</label>
					<select name="user" value={this.state.selectedUser} onChange={this.handleUserChange} required>
						<option value="">-- Select option --</option>
						{options}
					</select>
				</div>
				<div key="description" className="form-group">
					<label htmlFor="description">Description</label>
					<textarea ref="description" className="form-control" placeholder="description" maxLength="60"/>
				</div>
			</div>
		);

		return (
			<div>
				<a href="#createSchedule" data-toggle="modal" data-target="#createSchedule">Create</a>
				<div className="modal fade" id="createSchedule">
				  <div className="modal-dialog">
					<div className="modal-content">
					  {/* Modal Header */}
					  <div className="modal-header">
						<h4 className="modal-title">Create new schedule</h4>
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
		this.handleUserChange = this.handleUserChange.bind(this);
		
		this.state = {
			active: this.props.schedule.entity["active"],
			selectedUser: this.props.schedule.entity["user"].id
		};
	}

	handleActiveChange(e) {
		e.preventDefault();
		
	    this.setState({active: e.target.value});
	};

	handleUserChange(e) {
		e.preventDefault();
		
		this.setState({selectedUser: e.target.value});
	};

	handleSubmit(e) {
		e.preventDefault();
		
		const form = ReactDOM.findDOMNode(this.refs["form"]);
		if (form.checkValidity()) {
			const updatedSchedule = {};
			this.props.attributes.forEach(attribute => {
				if (attribute == "active") {
					updatedSchedule[attribute] = this.state.active;
				}
				else if (attribute == "user") {
					const id = this.state.selectedUser;
					this.props.users.forEach(user => {
						if (id == user.id) {
							updatedSchedule[attribute] = user;
						}
					});
				}
				else {
					updatedSchedule[attribute] = ReactDOM.findDOMNode(this.refs[attribute]).value.trim();
				}
			});
			this.props.onUpdate(this.props.schedule, updatedSchedule, this.props.index);
		}
		else {
			form.querySelector('input[type="submit"]').click();
		}
	}

	render() {
		let options;
		if (this.props.users != undefined) {
			options = this.props.users.map((user) => 
				<option key={"userupdate" + user.id} value={user.id}>{user.name}</option>
			);
		}

		const inputs = (
			<div>
				<input type="hidden" ref="id" value={this.props.schedule.entity["id"]}/>
				<div key="name" className="form-group">
					<label htmlFor="name">Name</label>
					<input type="text" ref="name" className="form-control" placeholder="name" required minLength="1" maxLength="60" defaultValue={this.props.schedule.entity["name"]}/>
				</div>
				<div key="active" className="form-group">
					Active
					<label><input name="active" type="radio" value="1" checked={this.state.active == "1"} onChange={this.handleActiveChange}/> Yes</label>
					<label><input name="active" type="radio" value="0" checked={this.state.active == "0"} onChange={this.handleActiveChange}/> No</label>
				</div>
				<div key="user" className="form-group">
				    <label htmlFor="user">User</label>
					<select name="user" value={this.state.selectedUser} onChange={this.handleUserChange}>
						<option value="">-- Select option --</option>
						{options}
					</select>
				</div>
				<div key="description" className="form-group">
					<label htmlFor="description">Description</label>
					<input type="text" ref="description" className="form-control" placeholder="description" maxLength="60" defaultValue={this.props.schedule.entity["description"]}/>
				</div>
			</div>
		);

		return (
			<span key={this.props.schedule.entity._links.self.href}>
				<a href={"#update_" + this.props.index} data-toggle="modal" data-target={"#update_" + this.props.index}>Update</a>
				<div className="modal fade" id={"update_" + this.props.index}>
				  <div className="modal-dialog">
					<div className="modal-content">
					  {/* Modal Header */}
					  <div className="modal-header">
						<h4 className="modal-title">Update a schedule</h4>
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

export default ScheduleApp;