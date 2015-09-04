// https://raw.githubusercontent.com/rethinkdb/rethinkdb/v2.1.1/src/rdb_protocol/ql2.proto
object ApiDefinitions {

  def genGroupModule(termType: Int, name: String) = {
    module(termType = termType, name = name)(Top.Function, Top.Datum.Obj)(
      fun(Top.Sequence)(multiarg("field", Top.Datum.Str)),
      fun(name + "_f", Top.Sequence)(multiarg("f", Top.FunctionArg(1)))
    )
  }

  val modules = Seq(
    // * Data Operators
    // Returns a reference to a database.
    // STRING -> Database
    module(termType = 14, name = "db", doc =
      """
        |Reference a database.
        |Example: Explicitly specify a database for a query.
        |{{{
        |r.db('heroes').table('marvel').run(conn, callback)
        |}}}
      """.stripMargin
    )(Top.Database)(
        fun(arg("name", Top.Datum.Str))
      ),

    // TABLE = 15;
    // Database, STRING, {read_mode:STRING, identifier_format:STRING} -> Table
    // STRING, {read_mode:STRING, identifier_format:STRING} -> Table
    module(termType = 15, name = "table", doc =
      """
        |Select all documents in a table. This command can be chained with other commands to do further processing on the data.
        |Example: Return all documents in the table 'marvel' of the default database.
        |{{{
        |r.table('marvel').run(conn, callback)
        |}}}
      """.stripMargin
    )(Top.Sequence.Table)(
        fun(Top.Database)(
          arg("name", Top.Datum.Str),
          opt("read_mode", Top.Datum.Str),
          opt("identifier_format", Top.Datum.Str)
        ),
        fun(Top.Database)(
          arg("name", Top.Datum.Str)
        ),
        fun(Top.Database)(
          arg("name", Top.Datum.Str),
          opt("read_mode", Top.Datum.Str)
        )
      ),

    // Gets a single element from a table by its primary or a secondary key.
    // Table, STRING -> SingleSelection | Table, NUMBER -> SingleSelection |
    // Table, STRING -> NULL            | Table, NUMBER -> NULL |
    module(termType = 16, name = "get")(Top.Datum.SingleSelection)(
      fun(Top.Sequence.Table)(arg("key", Top.Datum.Str)),
      fun(Top.Sequence.Table)(arg("key", Top.Datum.Num))
    ),


    // GET_FIELD  = 31; // OBJECT, STRING -> DATUM
    // | Sequence, STRING -> Sequence
    module(termType = 31, name = "getField")(Top.AnyType)(
      fun(Top.Datum.Obj)(arg("field_name", Top.Datum.Str))
    ),

    module(termType = 38, name = "map", doc =
      """
        |Transform each element of one or more sequences by applying a mapping function to them. If map is run with two or more sequences, it will iterate for as many items as there are in the shortest sequence.
        |Example: Return the first five squares.
        |{{{
        |r.expr([1, 2, 3, 4, 5]).map(function (val) {
        |    return val.mul(val);
        |}).run(conn, callback);
        |}}}
        |// Result passed to callback
        |[1, 4, 9, 16, 25]
      """.stripMargin
    )(Top.Sequence)(
        fun(Top.Sequence)(arg("f", Top.FunctionArg(1)))
      ),

    // Table, OBJECT, {conflict:STRING, durability:STRING, return_changes:BOOL} -> OBJECT |
    // Table, Sequence, {conflict:STRING, durability:STRING, return_changes:BOOL} -> OBJECT
    module(termType = 56, name = "insert", doc =
      """Inserts into a table.  If `conflict` is replace, overwrites
        |entries with the same primary key.  If `conflict` is
        |update, does an update on the entry.  If `conflict` is
        |error, or is omitted, conflicts will trigger an error.
        |{{{
        |r.table("posts").insert({
        |    id: 1,
        |    title: "Lorem ipsum",
        |    content: "Dolor sit amet"
        |}).run(conn, callback)
        |}}}
      """.stripMargin
    )(Top.Datum.Obj)(
        fun(Top.Sequence.Table)(arg("data", Top.Datum.Obj)),
        fun(Top.Sequence.Table)(arg("data", Top.Sequence))
      ),

    // SEQUENCE, STRING -> GROUPED_SEQUENCE | SEQUENCE, FUNCTION -> GROUPED_SEQUENCE
    genGroupModule(144, "group"),
    genGroupModule(145, "sum"),
    genGroupModule(146, "avg"),
    genGroupModule(147, "min"),
    genGroupModule(148, "max"),

    module(termType = 152, name = "changes")
      (Top.Sequence.Stream)
      (fun(Top.Sequence.Table)()),

    //----------------------------------------------------
    //
    //  Work with time
    //
    //----------------------------------------------------

    module(termType = 103, name = "now")(Top.PseudoType.Time)(fun()),
    module(termType = 103, name = "inTimezone", doc =
      """
        |Puts a time into an ISO 8601 timezone.
      """.stripMargin)(Top.Datum)(
        fun(Top.PseudoType.Time)(arg("timezone", Top.Datum.Str))
      ),
    module(termType = 105, name = "during", doc =
      """
        |a.during(b, c) returns whether a is in the range [b, c]
      """.stripMargin)(Top.Datum.Bool)(
        fun(Top.PseudoType.Time)(arg("left", Top.PseudoType.Time), arg("right", Top.PseudoType.Time))
      ),
    module(termType = 106, name = "date", doc =
      """
        |Retrieves the date portion of a time.
      """.stripMargin)(Top.PseudoType.Time)(
        fun(Top.PseudoType.Time)()
      ),
    module(termType = 126, name = "timeOfDay", doc =
      """
        |x.time_of_day == x.date - x
      """.stripMargin)(Top.Datum.Num)(
        fun(Top.PseudoType.Time)()
      ),
    module(termType = 127, name = "timezone", doc =
      """
        |Returns the timezone of a time.
      """.stripMargin)(Top.Datum.Str)(
        fun(Top.PseudoType.Time)()
      ),

    module(termType = 128, name = "year")(Top.Datum.Num)(fun(Top.PseudoType.Time)()),
    module(termType = 129, name = "month")(Top.Datum.Num)(fun(Top.PseudoType.Time)()),
    module(termType = 130, name = "day")(Top.Datum.Num)(fun(Top.PseudoType.Time)()),
    module(termType = 131, name = "dayOfWeek")(Top.Datum.Num)(fun(Top.PseudoType.Time)()),
    module(termType = 132, name = "dayOfYear")(Top.Datum.Num)(fun(Top.PseudoType.Time)()),
    module(termType = 133, name = "hours")(Top.Datum.Num)(fun(Top.PseudoType.Time)()),
    module(termType = 134, name = "minutes")(Top.Datum.Num)(fun(Top.PseudoType.Time)()),
    module(termType = 135, name = "seconds")(Top.Datum.Num)(fun(Top.PseudoType.Time)()),

    module(termType = 136, name = "time", doc =
      """
        |time(year, month, day[, hour, minute, second], timezone)
        |
        |Create a time object for a specific time.
        |
        |A few restrictions exist on the arguments:
        |
        |  * year is an integer between 1400 and 9,999.
        |  * month is an integer between 1 and 12.
        |  * day is an integer between 1 and 31.
        |  * hour is an integer.
        |  * minutes is an integer.
        |  * seconds is a double. Its value will be rounded to three decimal places (millisecond-precision).
        |  * timezone can be 'Z' (for UTC) or a string with the format ±[hh]:[mm].
        |
        |Example:
        |
        |Update the birthdate of the user "John" to November 3rd, 1986 UTC.
        |{{{
        |r.table("user").get("John").update({birthdate: r.time(1986, 11, 3, 'Z')})
        |    .run(conn, callback)
        |}}}
      """.stripMargin)(Top.PseudoType.Time)(
        fun(
          arg("year", Top.Datum.Num),
          arg("month", Top.Datum.Num),
          arg("day", Top.Datum.Num),
          arg("timezone", Top.Datum.Str)
        ),
        fun(
          arg("year", Top.Datum.Num),
          arg("month", Top.Datum.Num),
          arg("day", Top.Datum.Num),
          arg("hour", Top.Datum.Num),
          arg("minutes", Top.Datum.Num),
          arg("seconds", Top.Datum.Num),
          arg("timezone", Top.Datum.Str)
        )
      )

    /*
        IMPLEMENT ME

        // Constants for ISO 8601 days of the week.
        MONDAY = 107;    // -> 1
        TUESDAY = 108;   // -> 2
        WEDNESDAY = 109; // -> 3
        THURSDAY = 110;  // -> 4
        FRIDAY = 111;    // -> 5
        SATURDAY = 112;  // -> 6
        SUNDAY = 113;    // -> 7

        // Constants for ISO 8601 months.
        JANUARY = 114;   // -> 1
        FEBRUARY = 115;  // -> 2
        MARCH = 116;     // -> 3
        APRIL = 117;     // -> 4
        MAY = 118;       // -> 5
        JUNE = 119;      // -> 6
        JULY = 120;      // -> 7
        AUGUST = 121;    // -> 8
        SEPTEMBER = 122; // -> 9
        OCTOBER = 123;   // -> 10
        NOVEMBER = 124;  // -> 11
        DECEMBER = 125;  // -> 12

     */
  )


}
