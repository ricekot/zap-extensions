import React from "react";
import reactDom from "react-dom";
import Root from "./routes/root";
import "./index.css";
import {
    createBrowserRouter,
    RouterProvider,
} from "react-router-dom";
import ErrorPage from "./error-page";
import Settings from "./routes/settings";
import RequestResponseViewer from "./routes/request-response";
import ScriptConsole from "./routes/script-console";

const router = createBrowserRouter([
    {
        path: "/",
        element: <Root />,
        errorElement: <ErrorPage />,
        children: [
            {
                path: "settings",
                element: <Settings />,
            },
            {
                path: "request-response",
                element: <RequestResponseViewer />
            },
            {
                path: "script-console",
                element: <ScriptConsole />
            }
        ]
    },
], {
    basename: import.meta.env.BASE_URL?.replace(/\/$/, "")
});

reactDom.render(
    <React.StrictMode>
        <RouterProvider router={router} />
    </React.StrictMode>,
    document.getElementById("root")
);
