import { useState } from "react";
const App = () => {
  const Menus = [
    { title: "Sites Tree", src: "globe" },
    { title: "Script Console", src: "script" },
  ];

  return (
    <div className="flex">
      <div
        className="flex flex-col w-20 bg-slate-800 h-screen p-3 relative"
      >
        <div className="flex-none">
          <ul>
            <li
              className="flex rounded-md p-2 cursor-pointer hover:bg-light-white items-center mt-2 focus:bg-light-white"
              title="Sites Tree"
            >
              <img src="./src/assets/globe2.svg" />
            </li>

            <li
              className="flex rounded-md p-2 cursor-pointer hover:bg-light-white items-center mt-2"
              title="Script Console"
            >
              <img src="./src/assets/code.svg" />
            </li>

            <li
              className="flex rounded-md p-2 cursor-pointer hover:bg-light-white items-center mt-2"
              title="Automation Framework"
            >
              <img src="./src/assets/automation.svg" />
            </li>
          </ul>
        </div>

        {/* Fill the space */}
        <div className="flex-1"></div>


        {/* Bottom Menu */}
        <div className="flex-none">
          <ul>
            <li
              className="flex rounded-md p-2 cursor-pointer hover:bg-light-white items-center mt-2"
              title="Settings"
            >
              <img src="./src/assets/gear.svg" />
            </li>
            <li
              className="flex rounded-md p-2 items-center mt-2"
              title="Zed Attack Proxy"
            >
              <img
                src="./src/assets/zap.svg"
              />
            </li>
          </ul>
        </div>

      </div>
      <div className="h-screen flex-1 p-7">
        <h1 className="text-2xl font-semibold ">Home Page</h1>
      </div>
    </div>
  );
};
export default App;
