package kernel.runtime

trait Responder[S,T] {
  def respond(req:S):T
}
