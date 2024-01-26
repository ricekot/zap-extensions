import { useState } from "react";
import { Link, Outlet } from "react-router-dom";

export default function WorkbenchTabs() {

    const MenuItems = [
        { title: "Request & Response", id: "request-response", description: "" },
        { title: "Script Console", id: "script-console", description: "" },
    ];

    const [activeTab, setActiveTab] = useState(null);

    const toggleActiveTab = (tab) => {
        setActiveTab(activeTab == tab ? null : tab)
    }

    return (
        <div className="flex flex-row flex-none bg-slate-700">
            {MenuItems.map((item, index) => (
                <Link key={index} to={item.id}>
                    <span
                        key={index}
                        className={`flex rounded-md h-14 p-2 cursor-pointer text-gray-300 text-base font-bold hover:bg-light-white items-center mt-2 mb-2 ml-2 mr-0}
                                            ${activeTab == item.id ? "bg-light-white" : ""}`}
                        title={item.description}
                        onClick={() => toggleActiveTab(item.id)}
                    >
                        <h3>{item.title}</h3>
                    </span>
                </Link>
            )
            )}
        </div>
    )
}