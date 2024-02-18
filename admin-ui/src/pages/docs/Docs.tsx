import './docs.scss';
import { RedocStandalone } from 'redoc';

const Docs = () => {
  return (
    <div className="docs">
      <RedocStandalone specUrl="http://localhost:8088/v3/api-docs"
      />
    </div>
  )
}

export default Docs;
