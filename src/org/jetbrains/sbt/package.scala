package org.jetbrains

import com.intellij.util.{Function => IdeaFunction, PathUtil}
import com.intellij.openapi.util.{Pair => IdeaPair}

import reflect.ClassTag
import java.io.File
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil
import java.lang.{Boolean => JavaBoolean}

/**
 * @author Pavel Fatin
 */
package object sbt {
  implicit def toIdeaFunction1[A, B](f: A => B): IdeaFunction[A, B] = new IdeaFunction[A, B] {
    def fun(a: A) = f(a)
  }

  implicit def toIdeaPredicate[A](f: A => Boolean): IdeaFunction[A, JavaBoolean] = new IdeaFunction[A, JavaBoolean] {
    def fun(a: A) = JavaBoolean.valueOf(f(a))
  }

  implicit def toIdeaFunction2[A, B, C](f: (A, B) => C): IdeaFunction[IdeaPair[A, B], C] = new IdeaFunction[IdeaPair[A, B], C] {
    def fun(pair: IdeaPair[A, B]) = f(pair.getFirst, pair.getSecond)
  }

  implicit class RichFile(file: File) {
    def /(path: String): File = new File(file, path)

    def `<<`: File = << (1)

    def `<<`(level: Int): File = RichFile.parent(file, level)

    def path: String = file.getPath

    def absolutePath: String = file.getAbsolutePath

    def canonicalPath = ExternalSystemApiUtil.toCanonicalPath(file.getAbsolutePath)
  }

  private object RichFile {
    def parent(file: File, level: Int): File =
      if (level > 0) parent(file.getParentFile, level - 1) else file
  }

  implicit class RichString(path: String) {
    def toFile: File = new File(path)
  }

  implicit class RichBoolean(val b: Boolean) {
    def option[A](a: => A): Option[A] = if(b) Some(a) else None

    def either[A, B](right: => B)(left: => A): Either[A, B] = if (b) Right(right) else Left(left)
  }

  def jarWith[T : ClassTag]: File = {
    val tClass = implicitly[ClassTag[T]].runtimeClass

    Option(PathUtil.getJarPathForClass(tClass)).map(new File(_)).getOrElse {
      throw new RuntimeException("Jar file not found for class " + tClass.getName)
    }
  }
}
