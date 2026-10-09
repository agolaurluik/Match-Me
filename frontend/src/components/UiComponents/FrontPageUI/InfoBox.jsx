const InfoBox = ({side = 'left', title, text, children, marginTop}) => {
    return (
    <div className={`info-box ${side}`}>
        <div className="info-content">
            <h2 className="infobox-title">{title}</h2>
            <p>{text}</p>
            {children && <div style={{ marginTop: '1.5rem' }}>{children}</div>}
        </div>
    </div>      
    );
};

export default InfoBox
