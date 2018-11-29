package com.shapesecurity.shift.es2017.semantics;

public enum BrokenThrough {
	TRY_WITHOUT_FINALLY,
	TRY_WITH_FINALLY,
	FINALLY
	// We don't include "catch" because we are not aware of a consumer which needs that information, but it would be fine to include also
}
