const React = require('react');
const ReactDOM = require('react-dom');
import { BrowserRouter, Switch, Route, Link } from "react-router-dom";

import LoginApp from './login'; 
import ScheduleApp from './schedule'; 
import UserApp from './user'; 

class App extends React.Component {
	render() {
		return (
		  <div>
			<Header />
			<Main />
		  </div>
		)
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

class Main extends React.Component {
	render() {
		return (
			<main>
				<Switch>
				  <Route exact path='/' render={() => <LoginApp/>}/>
				  <Route path='/users' render={() => <UserApp/>}/>
				  <Route path='/schedules' render={() => <ScheduleApp/>}/>
				</Switch>
			</main>
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
