import { useEffect, useState } from 'react';
import { loadImageWithToken } from '../api/api';

export function useSecureImage(imagePath) {
    const [imageUrl, setImageUrl] = useState(null);


    useEffect(() => {
        let isMounted = true;

        const isValidPath = imagePath && typeof imagePath === 'string' && imagePath.trim() !== '';

        if (isValidPath ) {
            if(imagePath==="default-user.jpg"){
                            loadImageWithToken(`http://localhost:8080/api/image/public/${imagePath}`)
                .then(blobUrl => {
                    if (isMounted) setImageUrl(blobUrl);
                })
                .catch(err => {
                    console.error("Image fetch failed:", err);
                    if (isMounted) setImageUrl(null);
                });
                                            // console.log(imagePath)
            } else {
            loadImageWithToken(`http://localhost:8080/api/image/private/${imagePath}`)
                .then(blobUrl => {
                    if (isMounted) setImageUrl(blobUrl);
                })
                .catch(err => {
                    console.error("Image fetch failed:", err);
                    if (isMounted) setImageUrl(null);
                });
            }
            // console.log(imagePath)
        }

        return () => {
            isMounted = false;
        };
    }, [imagePath]);

    return imageUrl;
}