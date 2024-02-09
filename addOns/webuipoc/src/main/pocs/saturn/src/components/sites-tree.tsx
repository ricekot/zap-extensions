import React, { useState, useEffect } from "react";
import socket from "../socket";
import { Tree } from "react-arborist";

function addChildNode(parent, child) {
  console.log(parent, child)
  if (!parent.children) {
    parent.children = [];
  }
  for (let i = 0; i < parent.children.length; i++) {
    if (parent.children[i].id === child.id) {
      if (child.children) {
        addChildNode(parent.children[i], child.children[0]);
      }
      return;
    }
  }
  parent.children.push(child);
}

export default function SitesTree() {

  const [data, setData] = useState([{ "id": "root", "name": "Sites Tree", "children": [] }]);

  socket.addEventListener("open", (event) => {
    socket.send(JSON.stringify({ type: "sitesTree.getFullTree", message: null }));
  });
  
  socket.addEventListener("message", (event) => {
    const newData = JSON.parse(event.data);
    if (newData.type === "siteNode.added") {
      const rootNode = data[0];
      addChildNode(rootNode, newData.message);
      setData([rootNode]);
    }
  });

  return (
    <div className="pl-2 h-screen">
      <Tree
        data={data}
        disableDrag={true}
        disableDrop={true}
        disableEdit={true}
      />
    </div>
  );
}
