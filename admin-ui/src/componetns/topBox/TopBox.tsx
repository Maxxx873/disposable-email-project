import "./topBox.scss"
import { topDomains } from "../../data"

const TopBox = () => {
    return (
        <div className="topBox">
            <h1>Top Domains</h1>
            <div className="list">
                {topDomains.map(domain => (
                    <div className="listItem" key={domain.id}>
                        <div className="domain">
                            <div className="topDomains">
                                <span className="domainName">{domain.domain}</span>
                            </div>
                        </div>
                        <span className="amount">{domain.amount}</span>
                    </div>
                ))}
            </div>
        </div>
    )
}

export default TopBox