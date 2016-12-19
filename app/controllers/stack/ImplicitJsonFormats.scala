package controllers.stack

import net.liftweb.json.ext.JodaTimeSerializers
import net.liftweb.json.DefaultFormats
import java.text.SimpleDateFormat

trait ImplicitJsonFormats {

    private val default: DefaultFormats with Object {def dateFormatter: SimpleDateFormat} = new DefaultFormats {
        override def dateFormatter = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
    }
    implicit val liftJsonFormats = default ++ JodaTimeSerializers.all
}
