import Home from "./pages/home/Home"
import { createRoot } from "react-dom/client";
import {
  createBrowserRouter,
  RouterProvider,
  Route,
  Link,
} from "react-router-dom";
import Accounts from "./pages/accounts/Accounts";
import Domains from "./pages/domains/Domains";
function App() {

  const router = createBrowserRouter([
    {
      path: "/",
      element: (
        <Home />
      ),
    },
    {
      path: "accounts",
      element: <Accounts />,
    },
    {
      path: "domains",
      element: <Domains />,
    },
  ]);
  return <RouterProvider router={router}/>;
}

export default App
