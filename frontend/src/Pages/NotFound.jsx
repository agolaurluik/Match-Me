import './PagesCSS/NotFound.css'; 

const NotFound = () => {
  return (
    <div className="notfound-container">
      <img
        src="/public/bird.png" 
        alt="Page not found"
        className="notfound-image"
      />
      <h1>404 - Page Not Found</h1>
      <p>Sorry, the page you are looking for does not exist.</p>
    </div>
  );
};

export default NotFound;