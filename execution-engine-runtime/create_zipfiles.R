

testnames <- list.files(here::here("tests"))

for (i in seq_along(testnames)) {
  message(paste("writing ", testnames[i]))
  withr::with_dir(here::here("tests"), {
    filenames <- list.files(testnames[i], full.names = T)
    zip(zipfile = paste0("../zip/", testnames[i]), files = filenames)
  })
}



