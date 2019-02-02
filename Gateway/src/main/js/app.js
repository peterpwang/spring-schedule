const React = require('react');
const ReactDOM = require('react-dom');
import { BrowserRouter, Switch, Route, Link } from "react-router-dom";

import LoginApp from './login'; 
import ScheduleApp from './schedule'; 
import UserApp from './user'; 
import LoginContext from './logincontext';

class App extends React.Component {
	
	constructor(props) {
		super(props);

		this.toggleLogin = this.toggleLogin.bind(this);		
		this.state = {
            authorization: undefined,
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
			  <Route path='/users' render={() => {
				  return (<div><Header/><main><UserApp/></main></div>);
				}
			  }/>
			  <Route path='/schedules' render={() => {
				  return (<div><Header/><main><ScheduleApp/></main></div>);
				}
			  }/>
			</Switch>
		  </div>
		)
	}
	
	toggleLogin(newAuthorization) {
		this.setState(state => ({
			authorization: newAuthorization
        }));
	}
}

class Header extends React.Component {
	render() {
		return (
			<header>
				<nav>
				  <ul className="nav nav-tabs">
					<li className="nav-item">
					  <Link className="nav-link" to="/">Home</Link>
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
