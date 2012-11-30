source("../../analysis/load.r")
r <- readcsv("ScenarioExp.csv")
attach(r)

argFiltered <- arg[arg > 0]
counterArgFiltered <- counterArg[arg > 0]
Conflicts2 <- Conflicts[arg > 0]
A2 <- A[arg > 0]
R2 <- R[arg > 0]
B_s2 <- B_s[arg > 0]
O_s2 <- O_s[arg > 0]
G_s2 <- G_s[arg > 0]
O_r2 <- O_r[arg > 0]
G_r2 <- G_r[arg > 0]
l2 <- l[arg > 0]
G_nro2 <- G_nro[arg > 0]
B_ra2 <- B_ra[arg > 0]
B_nra2 <- B_nra[arg > 0]

argToGdFiltered <- argToGd[argToGd > 0]
counterArgToGdFiltered <- counterArgToGd[argToGd > 0]
Conflicts3 <- Conflicts[argToGd > 0]
A3 <- A[argToGd > 0]
R3 <- R[argToGd > 0]
B_s3 <- B_s[argToGd > 0]
O_s3 <- O_s[argToGd > 0]
G_s3 <- G_s[argToGd > 0]
O_r3 <- O_r[argToGd > 0]
G_r3 <- G_r[argToGd > 0]
l3 <- l[argToGd > 0]
G_nro3 <- G_nro[argToGd > 0]
B_ra3 <- B_ra[argToGd > 0]
B_nra3 <- B_nra[argToGd > 0]

oj <- arg/O
coj <- counterArgFiltered/argFiltered
op <- argToGd/O
cop <- counterArgToGdFiltered/argToGdFiltered

# Means and standard deviations
mean(oj[Conflicts=="Direct"])
sd(oj[Conflicts=="Direct"])
mean(coj[Conflicts2=="Direct"])
sd(coj[Conflicts2=="Direct"])
mean(op[Conflicts=="Direct"])
sd(op[Conflicts=="Direct"])
mean(cop[Conflicts3=="Direct"])
sd(cop[Conflicts3=="Direct"])

mean(oj[Conflicts=="Chained"])
sd(oj[Conflicts=="Chained"])
mean(coj[Conflicts2=="Chained"])
sd(coj[Conflicts2=="Chained"])
mean(op[Conflicts=="Chained"])
sd(op[Conflicts=="Chained"])
mean(cop[Conflicts3=="Chained"])
sd(cop[Conflicts3=="Chained"])

mean(oj)
sd(oj)
mean(coj)
sd(coj)
mean(op)
sd(op)
mean(cop)
sd(cop)

# Lineair model
oji <- lm(oj ~ Conflicts+A+R+B_s+O_s+G_s+O_r+G_r+l+G_nro+B_ra+B_nra)
coji <- lm(coj ~ Conflicts2+A2+R2+B_s2+O_s2+G_s2+O_r2+G_r2+l2+G_nro2+B_ra2+B_nra2)
opi <- lm(op ~ Conflicts+A+R+B_s+O_s+G_s+O_r+G_r+l+G_nro+B_ra+B_nra)
copi <- lm(cop ~ Conflicts3+A3+R3+B_s3+O_s3+G_s3+O_r3+G_r3+l3+G_nro3+B_ra3+B_nra3)

# Shows significance and unstandardized coefficients
summary(oji)
summary(coji)
summary(opi)
summary(copi)
# Beta coefficients
oji$coefficients["Conflicts"] * sd(Conflicts) / sd(oj)
oji$coefficients["A"] * sd(A) / sd(oj)
oji$coefficients["R"] * sd(R) / sd(oj)
oji$coefficients["B_s"] * sd(B_s) / sd(oj)
oji$coefficients["O_s"] * sd(O_s) / sd(oj)
oji$coefficients["G_s"] * sd(G_s) / sd(oj)
oji$coefficients["O_r"] * sd(O_r) / sd(oj)
oji$coefficients["G_r"] * sd(G_r) / sd(oj)
oji$coefficients["l"] * sd(l) / sd(oj)
oji$coefficients["G_nro"] * sd(G_nro) / sd(oj)
oji$coefficients["B_ra"] * sd(B_ra) / sd(oj)
oji$coefficients["B_nra"] * sd(B_nra) / sd(oj)
coji$coefficients["Conflicts2"] * sd(Conflicts2) / sd(coj)
coji$coefficients["A2"] * sd(A2) / sd(coj)
coji$coefficients["R2"] * sd(R2) / sd(coj)
coji$coefficients["B_s2"] * sd(B_s2) / sd(coj)
coji$coefficients["O_s2"] * sd(O_s2) / sd(coj)
coji$coefficients["G_s2"] * sd(G_s2) / sd(coj)
coji$coefficients["O_r2"] * sd(O_r2) / sd(coj)
coji$coefficients["G_r2"] * sd(G_r2) / sd(coj)
coji$coefficients["l2"] * sd(l2) / sd(coj)
coji$coefficients["G_nro2"] * sd(G_nro2) / sd(coj)
coji$coefficients["B_ra2"] * sd(B_ra2) / sd(coj)
coji$coefficients["B_nra2"] * sd(B_nra2) / sd(coj)

opi$coefficients["Conflicts"] * sd(Conflicts) / sd(op)
opi$coefficients["A"] * sd(A) / sd(op)
opi$coefficients["R"] * sd(R) / sd(op)
opi$coefficients["B_s"] * sd(B_s) / sd(op)
opi$coefficients["O_s"] * sd(O_s) / sd(op)
opi$coefficients["G_s"] * sd(G_s) / sd(op)
opi$coefficients["O_r"] * sd(O_r) / sd(op)
opi$coefficients["G_r"] * sd(G_r) / sd(op)
opi$coefficients["l"] * sd(l) / sd(op)
opi$coefficients["G_nro"] * sd(G_nro) / sd(op)
opi$coefficients["B_ra"] * sd(B_ra) / sd(op)
opi$coefficients["B_nra"] * sd(B_nra) / sd(op)
copi$coefficients["Conflicts3"] * sd(Conflicts3) / sd(cop)
copi$coefficients["A3"] * sd(A3) / sd(cop)
copi$coefficients["R3"] * sd(R3) / sd(cop)
copi$coefficients["B_s3"] * sd(B_s3) / sd(cop)
copi$coefficients["O_s3"] * sd(O_s3) / sd(cop)
copi$coefficients["G_s3"] * sd(G_s3) / sd(cop)
copi$coefficients["O_r3"] * sd(O_r3) / sd(cop)
copi$coefficients["G_r3"] * sd(G_r3) / sd(cop)
copi$coefficients["l3"] * sd(l3) / sd(cop)
copi$coefficients["G_nro3"] * sd(G_nro3) / sd(cop)
copi$coefficients["B_ra3"] * sd(B_ra3) / sd(cop)
copi$coefficients["B_nra3"] * sd(B_nra3) / sd(cop)


# Genereer dataset met voorspellingen volgens het model (met 100 datapunten)
p_oj <- predict(oji,list(x=seq(0,1000,1)))
p_coj <- predict(coji,list(x=seq(0,1000,1)))
p_op <- predict(opi,list(x=seq(0,1000,1)))
p_cop <- predict(copi,list(x=seq(0,1000,1)))

# Vind parameterinstelling bij een maximale voorspelde waarde
Conflicts[which(p_oj == max(p_oj))]
A[which(p_oj == max(p_oj))]
R[which(p_oj == max(p_oj))]
B_s[which(p_oj == max(p_oj))]
O_s[which(p_oj == max(p_oj))]
G_s[which(p_oj == max(p_oj))]
O_r[which(p_oj == max(p_oj))]
G_r[which(p_oj == max(p_oj))]
l[which(p_oj == max(p_oj))]
G_nro[which(p_oj == max(p_oj))]
B_ra[which(p_oj == max(p_oj))]
B_nra[which(p_oj == max(p_oj))]

# En voor tegenargumenten
Conflicts2[which(p_coj == max(p_coj))]
A2[which(p_coj == max(p_coj))]
R2[which(p_coj == max(p_coj))]
B_s2[which(p_coj == max(p_coj))]
O_s2[which(p_coj == max(p_coj))]
G_s2[which(p_coj == max(p_coj))]
O_r2[which(p_coj == max(p_coj))]
G_r2[which(p_coj == max(p_coj))]
l2[which(p_coj == max(p_coj))]
G_nro2[which(p_coj == max(p_coj))]
B_ra2[which(p_coj == max(p_coj))]
B_nra2[which(p_coj == max(p_coj))]

# En voor potential
Conflicts[which(p_op == max(p_op))]
A[which(p_op == max(p_op))]
R[which(p_op == max(p_op))]
B_s[which(p_op == max(p_op))]
O_s[which(p_op == max(p_op))]
G_s[which(p_op == max(p_op))]
O_r[which(p_op == max(p_op))]
G_r[which(p_op == max(p_op))]
l[which(p_op == max(p_op))]
G_nro[which(p_op == max(p_op))]
B_ra[which(p_op == max(p_op))]
B_nra[which(p_op == max(p_op))]

# En voor tegenargumenten
Conflicts3[which(p_cop == max(p_cop))]
A3[which(p_cop == max(p_cop))]
R3[which(p_cop == max(p_cop))]
B_s3[which(p_cop == max(p_cop))]
O_s3[which(p_cop == max(p_cop))]
G_s3[which(p_cop == max(p_cop))]
O_r3[which(p_cop == max(p_cop))]
G_r3[which(p_cop == max(p_cop))]
l3[which(p_cop == max(p_cop))]
G_nro3[which(p_cop == max(p_cop))]
B_ra3[which(p_cop == max(p_cop))]
B_nra3[which(p_cop == max(p_cop))]

# Wat zijn de maxima voor arg en tegen-arg?
max(p_oj)
max(p_coj)
max(p_op)
max(p_cop)

# Build and display tree model
library(tree)
tmodel <- tree(oj ~ A+R+B_s+O_s+G_s+O_r+G_r+l+B_nro+G_nro+B_ra+B_nra)
plot(tmodel)
text(tmodel)

# Pairs plot
pairs(oj ~ A+R+B_s+O_s+G_s+O_r+G_r+l+B_nro+G_nro+B_ra+B_nra, panel=panel.smooth)

# Curvature in data?
m1 <- lm(oj ~ A+I(A^2)+R+I(R^2)+B_s+I(B_s^2)+O_s+I(O_s^2)+G_s+I(G_s^2)+O_r+I(O_r^2)+G_r+I(G_r^2)+l+I(l^2)+B_nro+I(B_nro^2)+G_nro+I(G_nro^2)+B_ra+I(B_ra^2)+B_nra+I(B_nra^2))
