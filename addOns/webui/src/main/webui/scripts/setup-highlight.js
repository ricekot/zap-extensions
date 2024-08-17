import "highlight.js/styles/github.css";
import hljs from "highlight.js/lib/core";
import http from "highlight.js/lib/languages/http";
import json from "highlight.js/lib/languages/json";
import xml from "highlight.js/lib/languages/xml";
hljs.registerLanguage("http", http);
hljs.registerLanguage("json", json);
hljs.registerLanguage("xml", xml);
window.hljs = hljs;
