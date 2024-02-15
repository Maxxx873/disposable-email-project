import Home from "./pages/home/Home"
import {
  createBrowserRouter,
  RouterProvider,
  Outlet,
} from "react-router-dom";
import Accounts from "./pages/accounts/Accounts";
import Domains from "./pages/domains/Domains";
import Navbar from "./componetns/navbar/Navbar";
import Footer from "./componetns/footer/Footer";
import Menu from "./componetns/menu/Menu";
import Login from "./pages/login/Login";
import "./styles/global.scss"
import Domain from "./pages/domain/Domain";
import Account from "./pages/account/Account";
import Docs from "./pages/docs/Docs";

function App() {

  const Layout = () => {
    return (
      <div className="main">
        <Navbar />
        <div className="container">
          <div className="menuContainer">
            <Menu />
          </div>
          <div className="contentContainer">
            <Outlet />
          </div>
        </div>
        <Footer />
      </div>
    )
  }

  const router = createBrowserRouter([
    {
      path: "/",
      element: <Layout />,
      children: [
        {
          path: "/",
          element: <Home />
        },
        {
          path: "/accounts",
          element: <Accounts />,
        },
        {
          path: "/domains",
          element: <Domains />,
        },
        {
          path: "/accounts/:id",
          element: <Account />,
        },
        {
          path: "/domains/:id",
          element: <Domain />,
        },
        {
          path: "/docs/",
          element: <Docs />,
        }
      ]
    },
    {
      path: "/login",
      element: <Login />,
    }
  ]);
  return <RouterProvider router={router} />;
}

export default App
