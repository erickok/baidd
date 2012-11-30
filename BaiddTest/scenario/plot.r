source("../../analysis/load.r")
r <- readcsv("ScenarioExp.csv")
attach(r)

# Adjust plotFor to pick one of the parameters to plot
plotFor <- Conflicts
labelExp <- expression(l)
oj <- arg/O
argFiltered <- arg[arg > 0]
counterArgFiltered <- counterArg[arg > 0]
coj <- counterArgFiltered/argFiltered
x1 <- plotFor
x2 <- x1[arg > 0]

op <- argToGd/O
argToGdFiltered <- argToGd[argToGd > 0]
counterArgToGdFiltered <- counterArgToGd[argToGd > 0]
cop <- counterArgToGdFiltered/argToGdFiltered
x3 <- plotFor
x4 <- x1[argToGd > 0]

eb(x1, oj, expression(j[A]), labelExp)
eb(x2, coj , expression(bar(j)[A]),labelExp)
eb(x3, op, expression(j[A]^g[d]), labelExp)
eb(x4, cop , expression(bar(j)[A]^g[d]), labelExp)

# Old

#pairs(~ R + l + argForO,panel=panel.smooth)

#ConfDiff <- G_nro
#labels <- as.character(levels(factor(ConfDiff)))
#labels <- c('V1','V2')
#ebl(ConfDiff, argToGd/O, labels, "")
#ebl(ConfDiff, argForO/O, labels, "")
#ebl(ConfDiff, counterArgForO/O, labels, "")

