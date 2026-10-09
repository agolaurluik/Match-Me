import './SubHeader.css'

const SubHeader = ({activeTab, onTabChange}) => {

    const tabs = [
        { label: 'Chat', key: 'chat'},
        { label: 'Search Filter', key: 'filter'},
        { label: 'Recommendations', key: 'connections'},
        { label: 'Outgoing Connections', key: 'pending'},
        { label: 'Incoming Connections', key: 'incoming'},
    ];
    return (
        <div className="sub-header">
            <div className="sub-header-content">
                {tabs.map(tab => (
                    <button
                        key={tab.key}
                        onClick={() => onTabChange(tab.key)}
                        className={`sub-header-button ${activeTab === tab.key ? 'active' : ''}`}
                        >
                            {tab.label}
                        </button>
                ))}
            </div>
        </div>
    )
}

export default SubHeader;