package kernel.runtime

trait Listener[T] {
    def send(message:T):Unit
}
