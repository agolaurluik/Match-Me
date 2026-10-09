import './PagesCSS/Pages.css';
import './PagesCSS/FrontPage.css';


import InfoBox from '../components/UiComponents/FrontPageUI/InfoBox';
import { useState } from 'react';
import RegisterModal from '../components/Modals/RegisterModal';
import useScreenSize from '../hooks/useScreenSize';

const Frontpage = () => {
    const [showRegister, setShowRegister] = useState(false);
    const [showLogin, setShowLogin] = useState(false);

    const screenSize = useScreenSize();
    const isMobile = screenSize === 'mobile';

    const sections = [
        {
            title: "Welcome to WingLINK",
            text: "Choose why you're here — whether it's to make new friends, find collaborators, connect over shared hobbies, or explore something more. Your intent helps us find the right kind of people for you.",
        },
        {
            title: "Interests",
            text: "Let us know what you're into! From gaming and fitness to art and tech, your interests help us match you with people who genuinely vibe with your world.",
        },
        {
            title: "Personality",
            text: "Pick a few personality tags that reflect who you are — like outgoing, chill, ambitious, or deep thinker. This adds a personal touch to your profile and helps others connect beyond surface level.",
        },
        {
            title: "Location Range",
            text: "Set how far you're willing to connect — whether you're looking for people nearby or open to long-distance chats. The location range keeps your connections practical and meaningful.",
        },
        {
            title: "Ready to connect?",
            text: "Join the community and start matching with people who share your interests and personality.",
            hasButton: true,
        },
    ];

    const sectionImages = [
        ['heart.png'],
        ['book.png', 'football.png'],
        ['brain.png'],
        ['distance.png'],
        ['connections.png'],
    ];

    return (
        <main className="frontpage-wrapper">
            {sections.map((section, index) => (
                <section className="info-section" key={section.title}>
                    <InfoBox
                        side="left"
                        title={section.title}
                        text={section.text}
                    >
                        {section.hasButton && (
                            <button
                                className="register-button"
                                onClick={() => setShowRegister(true)}
                            >
                                Register Now
                            </button>
                        )}
                    </InfoBox>

                    <div
                        className="info-image-wrapper"
                        data-count={
                            isMobile ? 1 : sectionImages[index].length
                        }
                    >
                        {(isMobile
                            ? [sectionImages[index][0]]
                            : sectionImages[index]
                        ).map((src, imageIndex) => (
                            <img
                                key={src}
                                src={`/${src}`}
                                alt={`${section.title} illustration ${imageIndex + 1}`}
                                className="info-image"
                            />
                        ))}
                    </div>
                </section>
            ))}

            {showRegister && (
                <RegisterModal
                    onClose={() => setShowRegister(false)}
                />
            )}
        </main>
    );
};

export default Frontpage;

