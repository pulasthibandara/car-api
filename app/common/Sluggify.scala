package common

object Sluggify {
  def apply(input:String) = slugify(input)

  def slugify(input: String): String = {
    import java.text.Normalizer
    Normalizer.normalize(input, Normalizer.Form.NFD)
      .replaceAll("[^\\w\\s-]", "") // Remove all non-word, non-space or non-dash characters
      .replace('-', ' ')            // Replace dashes with spaces
      .trim                         // Trim leading/trailing whitespace (including what used to be leading/trailing dashes)
      .replaceAll("\\s+", "-")      // Replace whitespace (including newlines and repetitions) with single dashes
      .toLowerCase                  // Lowercase the final results
  }

  /**
    * Takes the string to be slugged and returns a slug that dones't conflict with
    * existing slugs.
    * @param sluggee string to be slugged.
    * @param slugged All slugs that exists starting with this slug.
    * @return slug, slug-1 or slug-999
    */
  def resolveSlug(sluggee: String, slugged: Seq[String]): String = {
    val slug = Sluggify(sluggee)

    slugStream(slug).filterNot(slugged.contains(_)).head
  }

  protected def slugStream(slug: String, next: Int = 0): Stream[String] = {
    val s = if(next == 0) slug else s"$slug-${next}"
    Stream(s).append(slugStream(slug, next + 1))
  }

  object StringOps {
    implicit class StringToSlug(s:String) {
      def slug = Sluggify(s)
    }
  }
}
