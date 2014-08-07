package models

/**
 * Created by jeroen on 4/3/14.
 */
trait JsonSchemas {
  val CUSTOMER_CREATE_SCHEMA =
    """
      |{
      |    "$schema": "http://json-schema.org/draft-04/schema#",
      |    "type": "object",
      |    "properties": {
      |        "name": {
      |            "type": "string",
      |            "minLength": 1
      |        },
      |        "login_name": {
      |            "type": "string",
      |            "minLength": 1
      |        },
      |         "password": {
      |            "type": "string",
      |            "minLength": 8,
      |            "pattern": "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?!.*[ ]).*$"
      |        }
      |    },
      |    "required": [
      |        "name", "login_name", "password"
      |    ],
      |    "additionalProperties": false
      |}
    """.stripMargin

  val USER_SEARCH_SCHEMA =
    """
      |{
      |    "$schema": "http://json-schema.org/draft-04/schema#",
      |    "type": "object",
      |    "properties": {
      |        "query": {
      |            "type": "object",
      |            "patternProperties": {
      |                "^.*$": {
      |                }
      |            },
      |          "additionalProperties": true
      |        },
      |        "filters": {
      |            "type": "array",
      |            "items": {
      |                "type": "object",
      |                "properties": {
      |                    "field": {
      |                        "type": "string"
      |                    },
      |                    "values": {
      |                        "type": "array",
      |                        "items": {
      |                        }
      |                    },
      |                    "range": {
      |                       "type": "boolean"
      |                    }
      |                },
      |                "required": [
      |                    "field",
      |                    "values"
      |                ]
      |            }
      |        },
      |        "facets": {
      |            "type": "array",
      |            "items": {
      |                "type": "object",
      |                "properties": {
      |                    "field": {
      |                        "type": "string"
      |                    },
      |                    "key": {
      |                        "type": "string"
      |                    },
      |                    "values": {
      |                        "type": "array",
      |                        "items": {
      |                        }
      |                    }
      |                },
      |                "required": [
      |                    "field"
      |                ]
      |            }
      |        }
      |    },
      |    "additionalProperties": false
      |}
    """.stripMargin

  val USER_FIELD_MAPPING_SCHEMA =
    """
      |{
      |    "$schema": "http://json-schema.org/draft-04/schema#",
      |    "type": "object",
      |    "properties": {
      |        "fields": {
      |            "type": "array",
      |            "uniqueItems": true,
      |            "items": {
      |                "type": "object",
      |                "patternProperties": {
      |                    "^.*$": {
      |                        "type": "object",
      |                        "properties": {
      |                            "type": {
      |                              "enum": [
      |                                    "string",
      |                                    "integer",
      |                                    "boolean",
      |                                    "long",
      |                                    "float",
      |                                    "double",
      |                                    "short",
      |                                    "byte",
      |                                    "date",
      |                                    "token_count",
      |                                    "binary"
      |                                ]
      |                            },
      |                          "store": { "type": "boolean"},
      |                          "postings_format" : {"type":"string"},
      |                          "null_value": {"type": "string"},
      |                            "index": {
      |                              "enum": [
      |                                    "analyzed",
      |                                    "not_analyzed",
      |                                    "no"
      |                                ]
      |                            },
      |                          "index_name": {"type": "string"},
      |                          "doc_values": {"type": "boolean"},
      |                            "term_vector": {
      |                                "enum": [
      |                                    "no",
      |                                    "yes",
      |                                    "with_offsets",
      |                                    "with_positions",
      |                                    "with_positions_offsets"
      |                                ]
      |                            },
      |                          "boost": {"type": "number"},
      |                            "norms": {
      |                                "type": "object",
      |                                "properties": {
      |                                  "enabled": {"type":"boolean"},
      |                                    "loading": {
      |                                        "enum": [
      |                                            "lazy",
      |                                            "eager"
      |                                        ]
      |                                    }
      |                                }
      |                            },
      |                            "index_options": {
      |                                "enum": [
      |                                    "docs",
      |                                    "freqs",
      |                                    "positions"
      |                                ]
      |                            },
      |                          "analyzer": {"type": "string"},
      |                          "index_analyzer": {"type": "string"},
      |                          "search_analyzer": {"type": "string"},
      |                          "include_in_all": {"type": "boolean"},
      |                          "ignore_above": {"type": "integer"},
      |                          "position_offset_gap": {"type": "integer"}
      |                        },
      |                        "required":["type"],
      |                        "additionalProperties": true
      |                    }
      |                }
      |            },
      |            "additionalProperties": true
      |        }
      |    },
      |    "additionalProperties": false
      |}
    """.stripMargin

  val schemaMap = Map(
    "user-search" -> USER_SEARCH_SCHEMA,
    "user-fieldmapping" -> USER_FIELD_MAPPING_SCHEMA,
    "customer-create" -> CUSTOMER_CREATE_SCHEMA
  )
}
