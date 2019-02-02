const React = require('react');
const ReactDOM = require('react-dom');
const when = require('when');
const client = require('./client');
const follow = require('./follow');
import LoginContext from './logincontext';

class LoginApp extends React.Component {

	constructor(props) {
		super(props);
		this.state = {
			loginError: undefined
		};
		this.onLogin = this.onLogin.bind(this);
		this.onLogout = this.onLogout.bind(this);
	}

	render() {
		if (this.context.authorization == undefined) {
			return (<LoginForm onLogin={this.onLogin} loginError={this.state.loginError} />);
		}
		else {
			return (<LogoutForm onLogout={this.onLogout}/>);
		}
	}

	onLogin(login) {
		client({
			method: 'POST',
			path: '/auth',
			entity: login,
			headers: {
				'Content-Type': 'application/json'
			}
		}).done(response => {
			this.context.toggleLogin(response.headers['Authorization']);
		}, response => {
			if (response.status.code === 403) {
				alert('ACCESS DENIED: You are not authorized.');
			} 
			else if (response.status.code === 500) {
				alert('Server error.');
			}
			else if (response.status.code === 400) {
				alert('Invalid data: ' + response.entity);
			}
		});
	}

	onLogout(logout) {
		client({
			method: 'POST',
			path: '/logout',
			headers: {
				'Content-Type': 'application/json'
			}
		}).done(response => {
			this.context.toggleLogin(null);
		}, response => {
			if (response.status.code === 403) {
				alert('ACCESS DENIED: You are not authorized to update.');
			} 
			else if (response.status.code === 500) {
				alert('Server error.');
			}
		});
	}
}

class LoginForm extends React.Component {

	constructor(props) {
		super(props);
		this.onLoginSubmit = this.onLoginSubmit.bind(this);
	}

	render() {
		const errorMessage = (this.props.loginError != undefined) ? (<p className="error">Wrong user or password</p>) : null;

		return (
			<div>
				<div className="jumbotron text-center">
					<h1>Login page</h1>
				</div>
				<div className="container">
					{errorMessage}
					<form ref="form">
						<div className="form-group">
							<label htmlFor="username">Username</label>
							<input type="text" ref="username" className="form-control" autoFocus="autofocus" placeholder="username" required/>
						</div>
						<div className="form-group">
							<label htmlFor="password">Password</label>
							<input type="password" ref="password" className="form-control" placeholder="password" required/>
						</div>
						<button type="button" className="btn" onClick={this.onLoginSubmit}>Log in</button>
						<input type="submit" ref="submit" className="hiddensubmit" value="Log in"/>
					</form>
				</div>
			</div>
		)
	}

	onLoginSubmit(e) {
		e.preventDefault();
		
		const form = ReactDOM.findDOMNode(this.refs["form"]);
		if (form.checkValidity()) {
			const username = ReactDOM.findDOMNode(this.refs["username"]).value.trim();
			const password = ReactDOM.findDOMNode(this.refs["password"]).value.trim();
			const login = {
				username: username, 
				password: password
			};
			this.props.onLogin(login);
		}
		else {
			form.querySelector('input[type="submit"]').click();
		}
	}
}

class LogoutForm extends React.Component {

	constructor(props) {
		super(props);
		this.onLogoutSubmit = this.onLogoutSubmit.bind(this);
	}

	render() {
		return (
			<div>
				Hello, <span id="managername">{this.props.loggedInManager}</span>.
				<form ref="form">
					<button type="button" className="btn" onClick={this.onLogoutSubmit}>Log out</button>
				</form>
			</div>
		)
	}

	onLogoutSubmit(e) {
		e.preventDefault();
		this.props.onLogout();
	}
}
	
LoginApp.contextType = LoginContext; // This part is important to access context values

export default LoginApp;