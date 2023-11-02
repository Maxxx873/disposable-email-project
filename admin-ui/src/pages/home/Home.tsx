import BigChartBox from "../../componetns/bigChartBox/BigChartBox"
import ChartBox from "../../componetns/chartBox/ChartBox"
import TopBox from "../../componetns/topBox/TopBox"
import { chartBoxDomain, chartBoxMessages, chartBoxAccount } from "../../data"
import "./home.scss"

const Home = () => {
    return (
        <div className="home">
            <div className="box box1">
                <TopBox />
            </div>
            <div className="box box2"><ChartBox {...chartBoxAccount} /></div>
            <div className="box box3"><ChartBox {...chartBoxDomain}/></div>
            <div className="box box6"><ChartBox {...chartBoxMessages}/></div>
            <div className="box box7"><BigChartBox /></div>
          </div>
    )
}

export default Home