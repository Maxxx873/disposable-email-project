import "./navbar.scss"
import { useAuth } from "react-oidc-context";

const Navbar = () => {
    const auth = useAuth();
    return (
        <div className="navbar">
            <div className="logo">
                <img src="logo.svg" alt="logo" />
                <span>Disposable Email</span>
            </div>
            <div className="icons">
                <img src="search.svg" alt="search" className="icon" />
                <img src="app.svg" alt="app" className="icon" />
                <img src="expand.svg" alt="expand" className="icon" />
                <div className="notification">
                    <img src="notification.svg" alt="notification" className="icon" />
                    <span>1</span>
                </div>
                <div className="user">
                    <img src="user.png" alt="user" className="icon" />
                    {<span>{auth.user?.profile.preferred_username}</span>}
                </div>
                <img src="settings.svg" alt="settings" className="icon" />
            </div>
        </div>
    )
}

export default Navbar