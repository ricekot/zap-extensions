import React from "react";
import CodeMirror from "@uiw/react-codemirror";
import { html } from "@codemirror/lang-html";
import { json } from "@codemirror/lang-json";

export default function RequestResponseViewer() {
  const exampleHtmlString = `<html>
    <head>
        <title>Example</title>
    </head>
    <body>
        <h1>Hello World</h1>
    </body>
</html> 
    `;

  return (
    <div className="flex flex-row">
      <div className="flex-col w-1/2">
        <div className="p-5">
          <h2>Request Header</h2>
          <CodeMirror editable={false} height="30vh" />
        </div>
        <div className="p-5">
          <h2>Request Body</h2>
          <CodeMirror
            value={exampleHtmlString}
            editable={false}
            height="45vh"
            extensions={[html(), json()]}
          />
        </div>
      </div>
      <div className="flex-col w-1/2">
        <div className="p-5">
          <h2>Response Header</h2>
          <CodeMirror editable={false} height="30vh" />
        </div>
        <div className="p-5">
          <h2>Response Body</h2>
          <CodeMirror
            value={exampleHtmlString}
            editable={false}
            height="45vh"
            extensions={[html(), json()]}
          />
        </div>
      </div>
    </div>
  );
}
