package fix

// format: off
package bar :
  def zzz: String = ???

  private object barImpl extends doobie.Aliases
  export barImpl.*
