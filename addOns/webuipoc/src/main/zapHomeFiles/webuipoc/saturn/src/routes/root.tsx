import { useState } from "react";
import { Link, Outlet } from "react-router-dom";
import WorkbenchTabs from "../components/workbench-tabs";
import SitesTree from "../components/sites-tree";
import ScriptsList from "../components/scripts-list";
import AutomationFramework from "../components/automation-framework";
import React from "react";

export default function Root() {

  type MenuItem = {
    id: string
    title: string
    iconSrc: string
    component?: JSX.Element
  }

  const TopMenuItems: MenuItem[] = [
    { id: "sites-tree", title: "Sites Tree", iconSrc: "globe2.svg", component: <SitesTree />, },
    { id: "scripts", title: "Scripts", iconSrc: "code.svg", component: <ScriptsList />, },
    { id: "automation-framework", title: "Automation Framework", iconSrc: "automation.svg", component: <AutomationFramework />, },
  ];

  const BottomMenuItems: MenuItem[] = [
    { id: "settings", title: "Settings", iconSrc: "gear.svg" },
  ]

  const [activeMenuItem, setActiveMenuItem] = useState(null);

  const toggleActiveMenuItem = (item: MenuItem) => {
    setActiveMenuItem(item?.id == activeMenuItem?.id ? null : item);
  }

  return (
    <div className="flex">
      <div
        className="flex flex-col flex-none w-16 bg-slate-800 h-screen p-1"
      >
        {/* Top Menu */}
        <div className="flex-none">
          <ul>
            {TopMenuItems.map((item, index) => (
              <li
                key={index}
                className={`flex rounded-md p-2 cursor-pointer hover:bg-light-white items-center 
                            ${index != 0 ? "mt-2" : ""}
                            ${activeMenuItem?.id == item.id ? "bg-light-white" : ""}`}
                title={item.title}
                onClick={() => toggleActiveMenuItem(item)}
              >
                <img src={`./src/assets/${item.iconSrc}`} />
              </li>
            )
            )}
          </ul>
        </div>

        {/* Fill the space */}
        <div className="flex-1"></div>

        {/* Bottom Menu */}
        <div className="flex-none">
          <ul>
            {BottomMenuItems.map((item, index) => (
              <Link key={index} to={item.id}>
                <li
                  key={index}
                  className={`flex rounded-md p-2 cursor-pointer hover:bg-light-white items-center mt-2
                            ${activeMenuItem?.id == item.id ? "bg-light-white" : ""}`}
                  title={item.title}
                >
                  <img src={`./src/assets/${item.iconSrc}`} />
                </li>
              </Link>
            )
            )}
            <Link to="/">
              <li
                className="flex rounded-md p-2 items-center mt-2"
                title="Zed Attack Proxy"
                onClick={() => setActiveMenuItem(null)}
              >
                <img
                  src="./src/assets/zap.svg"
                />
              </li>
            </Link>
          </ul>
        </div>

      </div>

      <div className={`flex-none bg-slate-300 w-96 ${activeMenuItem == null ? "hidden" : ""}`}>
        {activeMenuItem?.component}
      </div>

      <div className="flex flex-col flex-1">
        <WorkbenchTabs />
        <div className="flex-1">
          <Outlet />
        </div>
      </div>

    </div>
  );
};
