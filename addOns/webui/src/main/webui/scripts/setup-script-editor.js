import { basicSetup, EditorView } from "codemirror";
import { javascript } from "@codemirror/lang-javascript";

window.initZapScriptEditor = () => {
  if (window.scriptEditor) {
    return;
  }
  window.scriptEditor = new EditorView({
    extensions: [basicSetup, javascript()],
    parent: document.querySelector("#script-editor"),
  });
};
