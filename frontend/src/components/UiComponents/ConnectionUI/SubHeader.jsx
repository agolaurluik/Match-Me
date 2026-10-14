import './SubHeader.css';

const SubHeader = ({
    activeTab,
    onTabChange,
    showChatAlert = false,
    showIncomingAlert = false,
}) => {
    const tabs = [
        { label: 'Chat', key: 'chat', showAlert: showChatAlert },
        { label: 'Filter', key: 'filter' },
        { label: 'Discover', key: 'connections' },
        { label: 'Sent', key: 'pending' },
        { label: 'Received', key: 'incoming', showAlert: showIncomingAlert },
    ];

    return (
        <div className="sub-header">
            <div className="sub-header-content">
                {tabs.map(tab => (
                    <button
                        key={tab.key}
                        type="button"
                        onClick={() => onTabChange(tab.key)}
                        className={`sub-header-button ${activeTab === tab.key ? 'active' : ''
                            }`}
                    >
                        <span>{tab.label}</span>


                        {tab.showAlert && (
                            <span
                                className="sub-header-badge visible"
                                aria-label="New activity"
                            >
                                !
                            </span>
                        )}
                    </button>
                ))}
            </div>
        </div>
    );
};

export default SubHeader;
