package prowse.github.cosm1c.http.importer.util

object ReplyStatus {

    sealed trait Reply extends Serializable

    @SerialVersionUID(1L)
    final case object ReplySuccess extends Reply

    @SerialVersionUID(1L)
    final case object ReplyFailure extends Reply

}
