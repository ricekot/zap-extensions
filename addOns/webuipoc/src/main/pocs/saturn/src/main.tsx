import React from "react";
import ReactDOM from "react-dom/client";
import Root from "./routes/root";
import "./index.css";
import { createBrowserRouter, RouterProvider } from "react-router-dom";
import ErrorPage from "./error-page";
import Settings from "./routes/settings";
import RequestResponseViewer from "./routes/request-response";
import ScriptConsole from "./routes/script-console";

const router = createBrowserRouter(
  [
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
          element: <RequestResponseViewer />,
        },
        {
          path: "script-console",
          element: <ScriptConsole />,
        },
      ],
    },
  ],
  {
    basename: (import.meta as any).env.BASE_URL?.replace(/\/$/, ""),
  },
);

ReactDOM.createRoot(document.getElementById("root")).render(
  <React.StrictMode>
    <RouterProvider router={router} />
  </React.StrictMode>,
);
