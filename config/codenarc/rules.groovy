ruleset {
	ruleset('rulesets/basic.xml')
	ruleset('rulesets/braces.xml')
	ruleset('rulesets/exceptions.xml')
	ruleset('rulesets/imports.xml')
	ruleset('rulesets/logging.xml')
	ruleset('rulesets/naming.xml'){
		'MethodName' doNotApplyToClassNames: '*Spec'
	}
	ruleset('rulesets/unnecessary.xml')
	ruleset('rulesets/unused.xml')
}
