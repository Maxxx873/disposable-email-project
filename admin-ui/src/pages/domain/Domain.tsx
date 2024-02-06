import Single from "../../componetns/single/Single"
import { singleDomain } from "../../data"
import "./domain.scss"

const Domain = () => {

  //Fetch data and send to Single Component
  return (
    <div className="domain">
       <Single {...singleDomain}/>
    </div>
  )
}

export default Domain