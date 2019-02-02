const React = require('react');
const ReactDOM = require('react-dom');
const client = require('./client');
import {withRouter} from 'react-router-dom';
import LoginContext from './logincontext';

class LoginApp extends React.Component {

	constructor(props) {
		super(props);
		this.state = {
			loginError: undefined
		};
		this.onLogin = this.onLogin.bind(this);
	}

	render() {
		return (<LoginForm onLogin={this.onLogin} loginError={this.state.loginError} />);
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
			this.props.history.push('/profile'); //Redirected to profile page
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

export default withRouter(LoginApp);

LoginApp.contextType = LoginContext; // This part is important to access context values
