const React = require('react');

const LoginContext = React.createContext({
  authorization: undefined,
  toggleLogin: (newAuthorization) => {},
});

export default LoginContext;