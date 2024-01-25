import { useState } from "react";
import { Link, Outlet } from "react-router-dom";
const Root = () => {
  const TopMenuItems = [
    { title: "Sites Tree", link: "sites-tree", iconSrc: "globe2.svg" },
    { title: "Script Console", link: "script-console", iconSrc: "code.svg" },
    { title: "Automation Framework", link: "automation-framework", iconSrc: "automation.svg" },
  ];

  const BottomMenuItems = [
    { title: "Settings", link: "settings", iconSrc: "gear.svg" },
  ]

  const [activeTab, setActiveTab] = useState(null);

  const toggleActiveTab = (tab) => {
    setActiveTab(activeTab == tab ? null : tab)
  }

  return (
    <div className="flex">
      <div
        className="flex flex-col flex-none w-16 bg-slate-800 h-screen p-1"
      >
        {/* Top Menu */}
        <div className="flex-none">
          <ul>
            {TopMenuItems.map((item) => (
              <Link to={item.link}>
                <li
                  className={`flex rounded-md p-2 cursor-pointer hover:bg-light-white items-center mt-2
                            ${activeTab == item.link ? "bg-light-white" : ""}`}
                  title={item.title}
                  onClick={() => toggleActiveTab(item.link)}
                >
                  <img src={`./src/assets/${item.iconSrc}`} />
                </li>
              </Link>
            )
            )}
          </ul>
        </div>

        {/* Fill the space */}
        <div className="flex-1"></div>

        {/* Bottom Menu */}
        <div className="flex-none">
          <ul>
            {BottomMenuItems.map((item) => (
              <Link to={item.link}>
                <li
                  className={`flex rounded-md p-2 cursor-pointer hover:bg-light-white items-center mt-2
                            ${activeTab == item.link ? "bg-light-white" : ""}`}
                  title={item.title}
                  onClick={() => toggleActiveTab(item.link)}
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
                onClick={() => setActiveTab(null)}
              >
                <img
                  src="./src/assets/zap.svg"
                />
              </li>
            </Link>
          </ul>
        </div>

      </div>

      <div className={`flex-none bg-slate-300 w-96 ${activeTab == null ? "hidden" : ""}`}>
        <Outlet />
      </div>

      <div className="h-screen flex-1 p-7">
        <h1 className="text-2xl font-semibold ">Tabs!</h1>
      </div>
    </div>
  );
};
export default Root;
