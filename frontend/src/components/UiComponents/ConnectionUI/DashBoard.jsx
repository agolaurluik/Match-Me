import React, { useState } from 'react';
import SubHeader from './SubHeader';

import ChatTab from './ChatTab';
import './DashBoard.css'
import ConnectionsTab from './ConnectionsTab';
import PendingConnectionsTab from './PendingConnectionsTab';
import IncomingConnectionsTab from './IncomingConnectionsTab';
import FilterTab from './FilterTab';

const DashBoard = ({
        matchedUsersList = null,
    }
) => {

    
    const [activeTab, setActiveTab] = useState('chat');

    const renderContent = () => {
        switch (activeTab) {
            case 'chat':
            return <ChatTab/>;
            
            case 'filter':
            return <FilterTab
                    />

            case 'connections':
            return <ConnectionsTab
            />

            case 'pending':
            return <PendingConnectionsTab
                />

            case 'incoming':
            return <IncomingConnectionsTab
            />
            default:
                return null;
    }
    };

    return(
        <div className='dashboard-layout'>
            <SubHeader activeTab={activeTab} onTabChange={setActiveTab} />
            <div className="dashboard-body">
            {renderContent()}
            </div>
        </div>
    )
}

export default DashBoard;