import "./login.scss"
import { useAuth } from "react-oidc-context";

const Login = () => {
    const auth = useAuth();
    const onButtonClick = () => {
        auth.signinRedirect();
    }

    return (
        <div className="mainContainer">
            <div className={'titleContainer'}>
                <img src="user.png" alt="user" className="icon" />
                <div>Admin Dashboard</div>
            </div>
            <div>Disposable Email Project</div>
            <div className={'buttonContainer'}>
                <input
                    className={'inputButton'}
                    type="button"
                    onClick={onButtonClick}
                    value={auth.isAuthenticated ? 'Log out' : 'Log in'}
                />
                {auth.isAuthenticated ? <div>You logged as {auth.user?.profile.name}</div> : <div />}
            </div>
        </div>
    )
}

export default Login