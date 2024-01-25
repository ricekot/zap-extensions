import * as React from "react";
import reactDom from "react-dom";
import Root from "./routes/root";
import "./index.css";
import {
    createBrowserRouter,
    RouterProvider,
} from "react-router-dom";
import ErrorPage from "./error-page";
import SitesTree from "./routes/sites-tree";
import ScriptConsole from "./routes/script-console";
import AutomationFramework from "./routes/automation-framework";
import Settings from "./routes/settings";

const router = createBrowserRouter([
    {
        path: "/",
        element: <Root />,
        errorElement: <ErrorPage />,
        children: [
            {
                path: "/sites-tree",
                element: <SitesTree />,
            },
            {
                path: "/script-console",
                element: <ScriptConsole />,
            },
            {
                path: "/automation-framework",
                element: <AutomationFramework />,
            },
            {
                path: "/settings",
                element: <Settings />,
            }
        ]
    },
]);

reactDom.render(
    <React.StrictMode>
        <RouterProvider router={router} />
    </React.StrictMode>,
    document.getElementById("root")
);
