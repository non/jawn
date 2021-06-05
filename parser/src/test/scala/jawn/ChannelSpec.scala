package org.typelevel.jawn
package parser

import org.scalacheck.Properties

class ChannelSpec extends Properties("ChannelSpec") {

  property("large strings in files are ok") = {
    val M = 1000000
    val q = "\""
    val big = q + ("x" * (40 * M)) + q
    val bigEscaped = q + ("\\\\" * (20 * M)) + q

    val ok1 = TestUtil.withTemp(big)(t => Parser.parseFromFile(t)(Facade.NullFacade).isSuccess)

    val ok2 = TestUtil.withTemp(bigEscaped)(t => Parser.parseFromFile(t)(Facade.NullFacade).isSuccess)

    ok1 && ok2
  }
}
