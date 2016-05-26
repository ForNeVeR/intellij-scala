package org.jetbrains.plugins.scala.lang.transformation
package calls

import com.intellij.psi.PsiElement
import org.jetbrains.plugins.scala.extensions._
import org.jetbrains.plugins.scala.lang.psi.ScalaPsiElement
import org.jetbrains.plugins.scala.lang.psi.api.expr._
import org.jetbrains.plugins.scala.lang.psi.impl.ScalaCode._

/**
  * @author Pavel Fatin
  */
object ExpandDynamicCall extends AbstractTransformer {
  def transformation = {
    case e @ ScMethodCall(r @ RenamedReference(id, "applyDynamic"), _) =>
      r.replace(code"${r.qualifier.get}.applyDynamic(${quote(id)})")

    case e @ ScInfixExpr(l, RenamedReference(id, "applyDynamic"), r) =>
      e.replace(code"$l.applyDynamic(${quote(id)})($r)")

    case e @ ScMethodCall(r @ RenamedReference(id, "applyDynamicNamed"), assignments) =>
      e.replace(code"${r.qualifier.get}.applyDynamicNamed(${quote(id)})(${@@(assignments.map(asTuple))})")

    case e @ ScInfixExpr(l, RenamedReference(id, "applyDynamicNamed"), r) =>
      val assignments = r.breadthFirst.filter(_.isInstanceOf[ScAssignStmt]).toVector
      e.replace(code"$l.applyDynamicNamed(${quote(id)})(${@@(assignments.map(asTuple))})")

    case (e: ScReferenceExpression) && (r @ RenamedReference(id, "selectDynamic")) =>
      e.replace(code"${r.qualifier.get}.selectDynamic(${quote(id)})")

    // TODO fix an error in the implementation of resolve (must point to "selectDynamic")
    case e @ ScPostfixExpr(l, RenamedReference(id, "applyDynamic")) =>
      e.replace(code"$l.selectDynamic(${quote(id)})")

    case e @ ScAssignStmt(l @ RenamedReference(id, "updateDynamic"), Some(r)) =>
      e.replace(code"${l.qualifier.get}.updateDynamic(${quote(id)})($r)")

    // TODO fix an error in the implementation of resolve (must point to "updateDynamic")
    case e @ ScAssignStmt(ScPostfixExpr(l, RenamedReference(id, "applyDynamic")), Some(r)) =>
      e.replace(code"$l.updateDynamic(${quote(id)})($r)")
  }

  private def asTuple(assignment: PsiElement): ScalaPsiElement = assignment match {
    case ScAssignStmt(l, Some(r)) => code"(${quote(l.text)}, $r)"
  }
}