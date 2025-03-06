import {useEffect, useState} from "react";
import cloudyday from "./cloudy-day.svg";

export const WeatherHeader = () => {
    const [paragraphStyle, setStyle] = useState({
        fontSize: "10vw",
        lineHeight: "4vw",
        fontWeight: "200",
    });

    const updateStyle = () => {
        const vw = window.innerWidth * 0.07;
        const responsiveSize = vw;
        // console.log(responsiveSize)
        setStyle({
            fontSize: responsiveSize + "px",
            lineHeight: responsiveSize * 1.3 + "px",
            fontWeight: "200",
        });
    }

    useEffect(() => {
        updateStyle();
        window.addEventListener("resize", updateStyle);
        return () => window.removeEventListener("resize", updateStyle);
    }, []);

    return (
        <div className="Weather-header" style={paragraphStyle}>
            <img src={cloudyday} className="Weather-logo" alt="weather-logo"/>
            <span className="Header-text">weather</span>
        </div>
    );
}
