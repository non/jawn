package org.typelevel.claimant

import org.scalacheck.Prop

/**
 * A vastly simplified, macro-free version of Claimant for Dotty.
 */
object Claim {
  def apply(cond: Boolean): Prop = Prop(cond)
}
