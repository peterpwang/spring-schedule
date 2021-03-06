const React = require('react');
const ReactDOM = require('react-dom');
import { BrowserRouter, Switch, Route, Link } from "react-router-dom";

import LoginApp from './auth/login'; 
import ProfileApp from './auth/profile'; 
import ScheduleApp from './schedule/schedule'; 
import UserApp from './user/user'; 
import LoginContext from './public/logincontext';

class App extends React.Component {
	
	constructor(props) {
		super(props);

		this.toggleLogin = this.toggleLogin.bind(this);		
		this.state = {
            authorization: undefined,
			loggedInManager: undefined,
			toggleLogin: this.toggleLogin
		};
	}
	
	render() {
		return (
		  <div>
			<Switch>
			  <Route exact path='/' render={() => {
				  return (<LoginContext.Provider value={this.state}><LoginApp/></LoginContext.Provider>);
				}
			  }/>
			  <Route path='/profile' render={() => {
				  return (
				    <div>
					  <Header/>
					  <main><LoginContext.Provider value={this.state}><ProfileApp/></LoginContext.Provider></main>
					</div>
				  );
				}
			  }/>
			  <Route path='/users' render={() => {
				  return (
				    <div>
					  <Header/>
					  <main><LoginContext.Provider value={this.state}><UserApp/></LoginContext.Provider></main>
					</div>
				  );
				}
			  }/>
			  <Route path='/schedules' render={() => {
				  return (
				    <div>
					  <Header/>
					  <main><LoginContext.Provider value={this.state}><ScheduleApp/></LoginContext.Provider></main>
					</div>
				  );
				}
			  }/>
			</Switch>
		  </div>
		)
	}
	
	toggleLogin(newAuthorization, newLoggedInManager) {
		this.setState(state => ({
			authorization: newAuthorization,
			loggedInManager: newLoggedInManager
        }));
	}
}

class Header extends React.Component {
	render() {
		return (
			<div>
				<div>Worker's Schedule</div>
				<header>
					<nav>
					  <ul className="nav nav-tabs">
						<li className="nav-item">
						  <Link className="nav-link" to="/profile">Home</Link>
						</li>
						<li className="nav-item">
						  <Link className="nav-link" to="/users">User</Link>
						</li>
						<li className="nav-item">
						  <Link className="nav-link" to="/schedules">Schedule</Link>
						</li>
					  </ul>
					</nav>
				</header>
			</div>
		)
	}
}

ReactDOM.render(
	(<BrowserRouter>
	   <App/>
	 </BrowserRouter>
	),
	document.getElementById('react')
)
