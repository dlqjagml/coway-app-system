package assignment.cowaysystem.feature.order.entity

import assignment.cowaysystem.feature.order.entity.base.BaseTimeEntity
import javax.persistence.*

@Entity
@Table(name = "MEMBER")
class Member: BaseTimeEntity() {
    @Id
    @GeneratedValue
    @Column(name = "member_id")
    var uid: Long = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "authority_id")
    var authority: Authority? = null

    var username: String? = null

    @Column(unique = true)
    var email: String? = null

    var password: String? = null

    var address: String? = null
}