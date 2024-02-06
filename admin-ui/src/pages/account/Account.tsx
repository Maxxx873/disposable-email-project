import Single from "../../componetns/single/Single"
import { singleAccount } from "../../data"
import "./account.scss"

const Account = () => {

  //Fetch data and send to Single Component
  return (
    <div className="account">
       <Single {...singleAccount}/>
    </div>
  )
}

export default Account