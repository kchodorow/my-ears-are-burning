def chrome_extension(name, srcs):
  native.genrule(
      name = name,
      srcs = srcs + ["manifest.json"],
      outs = ["%s.zip" % name],
      cmd = "full_path=$$PWD/$@; cd %s && zip -r --quiet $$full_path *" % PACKAGE_NAME
  )
