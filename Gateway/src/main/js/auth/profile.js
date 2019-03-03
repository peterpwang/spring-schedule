const React = require('react');
const ReactDOM = require('react-dom');
const client = require('../public/client');
import {withRouter} from 'react-router-dom';
import LoginContext from '../public/logincontext';

class ProfileApp extends React.Component {

	constructor(props) {
		super(props);
		this.onLogout = this.onLogout.bind(this);
	}

	render() {
		return (<LogoutForm onLogout={this.onLogout} loggedInManager={this.context.loggedInManager}/>); //issue on reactjs v16.7.0
	}

	onLogout(logout) {
		client({
			method: 'POST',
			path: '/logout',
			headers: {
				'Content-Type': 'application/json'
			}
		}).done(response => {
			this.context.toggleLogin(null, null);
			this.props.history.push('/'); //Redirected to login page
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
	
export default withRouter(ProfileApp);

ProfileApp.contextType = LoginContext; // This part is important to access context values
